package com.crawljax.core;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.exception.CrawlPathToException;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.*;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.dataType.PlainElement;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.Helper;
import com.crawljax.util.SimHelper;
import com.crawljax.util.XPathHelper;
import main.java.lock.LockHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;

/**
 * Class that performs crawl actions. It is designed to be run inside a Thread.
 *
 * @see #run()
 * @author dannyroest@gmail.com (Danny Roest)
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @author Amin Milani Fard
 * @version $Id: Crawler.java 492M 2012-05-29 19:12:22Z (local) $
 */
public class Crawler implements Runnable {
	/**
	 * Added by Amin
	 * CrawlStrategy: different crawling strategies set in the guidedCrawl()
	 * pauseFlag: used for wait/resume in diverse crawling
	 * strategicCrawl: if true, runs guidedCrawl which uses different CrawlStrategy. This is false by default.
	 */
	// Amin: Different strategies for the guided crawling
			// 移到枚举类CrawlStrategy
//	enum CrawlStrategy {
//		DFS, BFS, Rand, Div
//	}
	private boolean pauseFlag = true;
	private boolean strategicCrawl = false;

	private Condition waitToCrawl = null;

	private int keywordPos;

	// 当前试探过程中涉及的状态
	private Stack<StateVertix> tryCrawlStack = new Stack<>();

	// 从index开始到当前状态，Crawler所经过的StateVertex
	private Stack<StateVertix> crawlStack = new Stack<>();

	private boolean onlyFireSpecificEvent = false;

	private static final Logger LOGGER = Logger.getLogger(Crawler.class.getName());

	private static final int ONE_SECOND = 1000;

	/**
	 * The main browser window 1 to 1 relation; Every Thread will get on browser assigned in the run
	 * function.
	 */
	private EmbeddedBrowser browser;

	/**
	 * The central DataController. This is a multiple to 1 relation Every Thread shares an instance
	 * of the same controller! All operations / fields used in the controller should be checked for
	 * thread safety.
	 */
	private final CrawljaxController controller;

	/**
	 * Depth register.
	 */
	private int depth = 0;

	/**
	 * The path followed from the index to the current state.
	 */
	private final CrawlPath backTrackPath;

	/**
	 * The utility which is used to extract the candidate clickables.
	 */
	private CandidateElementExtractor candidateExtractor;

	private boolean fired = false;

	/**
	 * The name of this Crawler when not default (automatic) this will be added to the Thread name
	 * in the thread as (name). In the {@link CrawlerExecutor#beforeExecute(Thread, Runnable)} the
	 * name is retrieved using the {@link #toString()} function.
	 *
	 * @see Crawler#toString()
	 * @see CrawlerExecutor#beforeExecute(Thread, Runnable)
	 */
	private String name = "";

	/**
	 * The sateMachine for this Crawler, keeping track of the path crawled by this Crawler.
	 */
	private final StateMachine stateMachine;

	private final CrawljaxConfigurationReader configurationReader;

	private FormHandler formHandler;

	/**
	 * The object to places calls to add new Crawlers or to remove one.
	 */
	private final CrawlQueueManager crawlQueueManager;

	/**
	 * Enum for describing what has happened after a {@link Crawler#clickTag(Eventable)} has been
	 * performed.
	 *
	 * @see Crawler#clickTag(Eventable)
	 */
	private enum ClickResult {
		cloneDetected, newState, domUnChanged
	}



	/**
	 * @param mother
	 *            the main CrawljaxController
	 * @param exactEventPath
	 *            the event path up till this moment.
	 * @param name
	 *            a name for this crawler (default is empty).
	 */
	public Crawler(CrawljaxController mother, List<Eventable> exactEventPath, String name, Integer keywordPos) {
		this(mother, new CrawlPath(exactEventPath), keywordPos);
		this.name = name;
		this.waitToCrawl = LockHelper.setNewCondition("waitToCrawl");
	}

	public Condition getWaitToCrawlCondition() {
	    return this.waitToCrawl;
    }

	/**
	 * Private Crawler constructor for a 'reload' crawler. Only used internally.
	 *
	 * @param mother
	 *            the main CrawljaxController
	 * @param returnPath
	 *            the path used to return to the last state, this can be a empty list
	 * @deprecated better to use {@link #Crawler(CrawljaxController, CrawlPath, Integer)}
	 */
	@Deprecated
	protected Crawler(CrawljaxController mother, List<Eventable> returnPath, Integer keywordPos) {
		this(mother, new CrawlPath(returnPath), keywordPos);
	}

	/**
	 * Private Crawler constructor for a 'reload' crawler. Only used internally.
	 *
	 * @param mother
	 *            the main CrawljaxController
	 * @param returnPath
	 *            the path used to return to the last state, this can be a empty list
	 */
	public Crawler(CrawljaxController mother, CrawlPath returnPath, Integer keywordPos) {
		this.keywordPos = keywordPos;
		// 从index到当前状态的路径
		this.backTrackPath = returnPath;
		// CrawljaxController
		this.controller = mother;
		// 配置读取类
		this.configurationReader = controller.getConfigurationReader();
		// 等于CrawljaxController本身
		this.crawlQueueManager = mother.getCrawlQueueManager();
		if (controller.getSession() != null) {
			this.stateMachine =
			        new StateMachine(controller.getSession().getStateFlowGraph(), controller
			                .getSession().getInitialState(), controller.getInvariantList());
			stateMachine.setEfficientCrawling(controller.isEfficientCrawling());
		} else {
			/**
			 * Reset the state machine to null, because there is no session where to load the
			 * stateFlowGraph from.
			 */
			this.stateMachine = null;
		}
	}

	public void setKeywordPos(int keywordPos) {
		this.keywordPos = keywordPos;
	}

	public void setOnlyFireSpecificEvent(boolean onlyFireSpecificEvent) {
		this.onlyFireSpecificEvent = onlyFireSpecificEvent;
	}

	// 爬取新状态时初始化crawlStack以及tryCrawlStack
	public void clearCrawlStack() {
		crawlStack.clear();
		tryCrawlStack.clear();
	}

	/**
	 * Brings the browser to the initial state.
	 */
	public void goToInitialURL() {
		LOGGER.info("Loading Page "
		        + configurationReader.getCrawlSpecificationReader().getSiteUrl());
		getBrowser().goToUrl(configurationReader.getCrawlSpecificationReader().getSiteUrl());
		/**
		 * Thread safe
		 */
		controller.doBrowserWait(getBrowser());
		CrawljaxPluginsUtil.runOnUrlLoadPlugins(getBrowser());
	}

	/**
	 * Try to fire a given event on the Browser.
	 *
	 * @param eventable
	 *            the eventable to fire
	 * @return true iff the event is fired
	 */
	private boolean fireEvent(Eventable eventable) {
		if (eventable.getIdentification().getHow().toString().equals("xpath")
		        && eventable.getRelatedFrame().equals("")) {

			/**
			 * The path in the page to the 'clickable' (link, div, span, etc)
			 */
			String xpath = eventable.getIdentification().getValue();

			/**
			 * The type of event to execute on the 'clickable' like onClick, mouseOver, hover, etc
			 */
			EventType eventType = eventable.getEventType();

			/**
			 * Try to find a 'better' / 'quicker' xpath
			 */
			String newXPath = new ElementResolver(eventable, getBrowser()).resolve();
			if (newXPath != null && !xpath.equals(newXPath)) {
				LOGGER.info("XPath changed from " + xpath + " to " + newXPath + " relatedFrame:"
				        + eventable.getRelatedFrame());
				eventable =
				        new Eventable(new Identification(Identification.How.xpath, newXPath),
				                eventType);
			}
		}

		if (getBrowser().fireEvent(eventable)) {

			/**
			 * Let the controller execute its specified wait operation on the browser thread safe.
			 */
			controller.doBrowserWait(getBrowser());

			/**
			 * Close opened windows
			 */

			getBrowser().closeOtherWindows();

			return true; // A event fired
		} else {
			/**
			 * Execute the OnFireEventFailedPlugins with the current crawlPath with the crawlPath
			 * removed 1 state to represent the path TO here.
			 */
			CrawljaxPluginsUtil.runOnFireEventFailedPlugins(eventable, controller.getSession()
			        .getCurrentCrawlPath().immutableCopy(true));
			return false; // no event fired
		}
	}

	/**
	 * Enters the form data. First, the related input elements (if any) to the eventable are filled
	 * in and then it tries to fill in the remaining input elements.
	 *
	 * @param eventable
	 *            the eventable element.
	 */
	private void handleInputElements(Eventable eventable) {
		List<FormInput> formInputs = eventable.getRelatedFormInputs();

		for (FormInput formInput : formHandler.getFormInputs()) {
			if (!formInputs.contains(formInput)) {
				formInputs.add(formInput);
			}
		}
		eventable.setRelatedFormInputs(formInputs);
		formHandler.handleFormElements(formInputs);
	}

	/**
	 * Reload the browser following the {@link #backTrackPath} to the given currentEvent.
	 *
	 * @throws CrawljaxException
	 *             if the {@link Eventable#getTargetStateVertix()} encounters an error.
	 */
	private void goBackExact() throws CrawljaxException {
		/**
		 * Thread safe
		 */
		StateVertix curState = controller.getSession().getInitialState();

		for (Eventable clickable : backTrackPath) {

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}

			LOGGER.info("Backtracking by executing " + clickable.getEventType() + " on element: "
			        + clickable);

			// 设置StateMachine跳转到clickable的目标状态，只是逻辑上的转变，浏览器并没有跳转
			this.getStateMachine().changeState(clickable.getTargetStateVertix());
			// 重设当前状态
			curState = clickable.getTargetStateVertix();

			controller.getSession().addEventableToCrawlPath(clickable);

			this.handleInputElements(clickable);

			// 真正执行浏览器跳转
			if (this.fireEvent(clickable)) {

				// TODO ali, do not increase depth if eventable is from guidedcrawling
				depth++;

				/**
				 * Run the onRevisitStateValidator(s)
				 */
				CrawljaxPluginsUtil.runOnRevisitStatePlugins(this.controller.getSession(),
				        curState);
			}

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}
		}
	}

	/**
	 * @param eventable
	 *            the element to execute an action on.
	 * @return the result of the click operation
	 * @throws CrawljaxException
	 *             an exception.
	 */
	private ClickResult clickTag(final Eventable eventable) throws CrawljaxException {

		LOGGER.info("Executing " + eventable.getEventType() + " on element: " + eventable
		        + "; State: " + this.getStateMachine().getCurrentState().getName());

		// 如果指定执行当前状态的某个元素，则不需要对爬取条件做变化
		if (!onlyFireSpecificEvent) {
			// load input element values
			this.handleInputElements(eventable);
			// Added by Amin: reducing from CandidateElements of the current state
			// 减少当前StateVertex的CandidateElement数
			this.getStateMachine().getCurrentState().decreaseCandidateElements();
			// check if all candidateElements have been fired on the current state
			// 如果当前StateVertex的CandidateElement数为0，则从notFullExpandedStates中删除当前状态
			if (this.getStateMachine().getCurrentState().isFullyExpanded())
				this.controller.getSession().getStateFlowGraph().removeFromNotFullExpandedStates(this.getStateMachine().getCurrentState());

			// 减去总的待执行事件
			CrawljaxController.NumCandidateClickables--;
		}

		// 如果成功执行该事件
		if (this.fireEvent(eventable)) {
			// 新建一个状态，注意到backTrackPath，实际上代表着到newState的父状态的路径，也就是当前状态
			StateVertix newState =
				new StateVertix(getBrowser().getCurrentUrl(), controller.getSession()
						.getStateFlowGraph().getNewStateName(), getBrowser().getDom(),
						this.controller.getStrippedDom(getBrowser()), backTrackPath);

			// domMutationNotifierPlugininCheck用于快速检测dom是否改变，因为dom对比是个耗时的过程
			// 默认false
			boolean domMutationNotifierPluginCheck = this.controller.getDomMutationNotifierPluginCheck();
			boolean isDomChanged=true;
			if (domMutationNotifierPluginCheck) {
				// One DomMutationNotifierPlugin has been instantiated

				isDomChanged = CrawljaxPluginsUtil.runDomMutationNotifierPlugin( browser );
			}

			if (domMutationNotifierPluginCheck==true && isDomChanged == false) {
				// One domMutationNotifierPlugin has been instantiated
				// Dom has not been changed so a time consuming Dom compare is not needed.
				return ClickResult.domUnChanged;

			}
			// 从这里开始执行
			else {
				// Either there is no domMutationNotifierPluginCheck or
				// Dom might have been changed. Thus a complete Dom comparison must be done
				// 检测是否为新状态，这里检测的是当前状态是否跳转到了另一个状态，如果当前状态跳转完之后还在当前状态，那么SFG无需更新
				if (CrawljaxPluginsUtil.runDomChangeNotifierPlugin(this.getStateMachine().getCurrentState(), eventable, newState)) {

					// Dom is changed, so data might need be filled in again

					// 当前crawler的爬取路径加上刚触发的eventable
					controller.getSession().addEventableToCrawlPath(eventable);
					// 更新StateMachine和SFG
					// 这里实际上会再进行一次重复性检测，这里检测的是当前状态跳转到的新状态是否存在于SFG
					// 注意两次检测的区别在于，第一次检测的针对前后两个状态，第二次检测是新状态在整个SFG的重复性检测
					// 如果该状态在SFG中已经存在，那么StateMachine的当前状态会指向SFG中的引用，这么做的意义重大，可以防止对同一个状态中的元素重复搜索
					if (this.getStateMachine().update(eventable, newState, this.getBrowser(),
							this.controller.getSession())) {
						// Dom changed
						// No Clone
						// TODO change the interface of runGuidedCrawlingPlugins to remove the
						// controller.getSession().getCurrentCrawlPath() call because its from the
						// session now.
						CrawljaxPluginsUtil.runGuidedCrawlingPlugins(controller, controller
								.getSession(), controller.getSession().getCurrentCrawlPath(), this
								.getStateMachine());

						// 发现新状态
						return ClickResult.newState;
					} else {
						// Dom changed; Clone
						return ClickResult.cloneDetected;
					}

				}

			}

		}

		// Event not fired or, Dom not changed

		// Amin: updating event productivity ratio for self-loop events or not fired ones
		if (controller.isEfficientCrawling())
			controller.getSession().getStateFlowGraph().updateEventProductivity(eventable, null);

		return ClickResult.domUnChanged;
	}

	/**
	 * Return the Exacteventpath.
	 *
	 * @return the exacteventpath
	 * @deprecated use {@link CrawlSession#getCurrentCrawlPath()}
	 */
	@Deprecated
	public final List<Eventable> getExacteventpath() {
		return controller.getSession().getCurrentCrawlPath();
	}

	/**
	 * Have we reached the depth limit?
	 *
	 * @param depth
	 *            the current depth. Added as argument so this call can be moved out if desired.
	 * @return true if the limit has been reached
	 */
	private boolean depthLimitReached(int depth) {

		if (this.depth >= configurationReader.getCrawlSpecificationReader().getDepth()
		        && configurationReader.getCrawlSpecificationReader().getDepth() != 0) {
			LOGGER.info("DEPTH " + depth + " reached returning from rec call. Given depth: "
			        + configurationReader.getCrawlSpecificationReader().getDepth());
			return true;
		} else {
			return false;
		}
	}

	// 将未注册的事件注册起来
	private void spawnThreads(StateVertix state) {
//		Crawler c = null;
		do {
//			if (c != null) {
//				//this.crawlQueueManager.addWorkToQueue(c);
//			}
//			c = new Crawler(this.controller, controller.getSession().getCurrentCrawlPath()
//			                .immutableCopy(true), null, this.keywordPos);
		} while (state.registerCrawler(this));
	}

	private ClickResult crawlAction(CandidateCrawlAction action) throws CrawljaxException {
		CandidateElement candidateElement = action.getCandidateElement();
		EventType eventType = action.getEventType();

		// orrigionalState存储了执行完任务之后到达的状态的前一个状态，目的是用于注册orrigionalState中尚未完成的任务
		StateVertix orrigionalState = this.getStateMachine().getCurrentState();

		// CandidateCrawlAction和eventType包装成了Eventable，送给clickTag去执行
		if (candidateElement.allConditionsSatisfied(getBrowser())) {
			ClickResult clickResult = clickTag(new Eventable(candidateElement, eventType));

			switch (clickResult) {
				case cloneDetected:
					fired = false;
					// We are in the clone state so we continue with the cloned version to search for work.
					// 其实此可的StateMachine已经指向了SFG中引用
					this.controller.getSession().branchCrawlPath();
					// 将上一个状态的未完成任务注册此来，此时的crawler已经进行更深一层的探测了
					// 所以原来一层的任务只能先包装起来
//					spawnThreads(orrigionalState);
					break;
				case newState:
					fired = true;
					// Recurse because new state found
//					spawnThreads(orrigionalState);

					//Amin: This is not used anymore due to single-threading diverse crawling
					//LOGGER.info("New state found!");
					// Amin: pause crawling if diverse crawling is set to true and if all browsers are opened
					//if (controller.isDiverseCrawling() && controller.allBrowsersOpened()){
					//	LOGGER.info("Thread will be paused for diverse crawling...");
					//	pauseFlag = true;
					//	pauseCrawling();
					//}

					break;
				case domUnChanged:
					// Dom not updated, continue with the next
					break;
				default:
					break;
			}
			return clickResult;
		} else {

			LOGGER.info("Conditions not satisfied for element: " + candidateElement + "; State: "
			        + this.getStateMachine().getCurrentState().getName());
		}
		return ClickResult.domUnChanged;
	}

	/**
	 * Crawl through the clickables.
	 *
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private boolean crawl() throws CrawljaxException {
		if (depthLimitReached(depth)) {
			return true;
		}

		if (!checkConstraints()) {
			return false;
		}

		// Store the currentState to be able to 'back-track' later.
		StateVertix orrigionalState = this.getStateMachine().getCurrentState();

		if (orrigionalState.searchForCandidateElements(candidateExtractor,
				configurationReader.getTagElements(), configurationReader.getExcludeTagElements(),
				configurationReader.getCrawlSpecificationReader().getClickOnce(),
				controller.getSession().getStateFlowGraph(), controller.isEfficientCrawling(), controller.isRandomEventExec(), false)) {
			// Only execute the preStateCrawlingPlugins when it's the first time
			LOGGER.info("Starting preStateCrawlingPlugins...");

			List<CandidateElement> candidateElements = orrigionalState.getUnprocessedCandidateElements();

			// Amin: just for logging
			// LOGGER.info("INNER # candidateElements for state " + orrigionalState.getName() + " is " + candidateElements.size());

			CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(),	candidateElements);
			// update crawlActions
			orrigionalState.filterCandidateActions(candidateElements);

			// Amin: This is the count of candidates after filtering...
			CrawljaxController.NumCandidateClickables += orrigionalState.getNumCandidateElements();
		}else {
			// Amin: just for logging
			 LOGGER.info("Outer # candidateElements for state " + orrigionalState.getName() + " is ZERO!");
		}

		// Amin: check if there were not candidateElements for the current state (i.e., is leaf node)
		if (orrigionalState.isFullyExpanded())
			this.controller.getSession().getStateFlowGraph().removeFromNotFullExpandedStates(orrigionalState);

		CandidateCrawlAction action =
			orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);


		while (action != null) {
			if (depthLimitReached(depth)) {
				return true;
			}

			if (!checkConstraints()) {
				return false;
			}
			ClickResult result = this.crawlAction(action);

			orrigionalState.finishedWorking(this, action);

			switch (result) {
				case newState:
					boolean detected = newStateDetected(orrigionalState);
					return detected;
				case cloneDetected:
					return true;
				default:
					break;
			}

			action = orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);
		}
		return true;
	}


	public boolean fireSpecificEvent(CandidateCrawlAction action) {
		if (depthLimitReached(depth)) {
			return true;
		}

		if (!checkConstraints()) {
			return false;
		}

		if (action != null) {
			try {
				ClickResult result = this.crawlAction(action);
				switch (result) {
					case newState:
						// 设置状态转换路径
						if (this.getStateMachine().getCurrentState().getCrawlPathToState().size() == 0) {
							this.getStateMachine().getCurrentState().setCrawlPathToState(controller.getSession().getCurrentCrawlPath());
						}
						break;
					case cloneDetected:
						/* 2020-1-4 */
						break;
					default:
						break;
				}
			} catch (CrawljaxException e) {
				System.out.println(e);
			}
		}
		return true;
	}
	/**
	 * Added by Amin for diverse crawling
	 * This is to pause a crawler after each time a new state is detected.
	 */
	void pauseCrawling(){
		try {
			synchronized(this){
				while(pauseFlag){// should not be here!!
					LOGGER.info("Wait until notified...");
					controller.addToWaitingCrawlerList(this);
					wait();
					pauseFlag = false;
					LOGGER.info("Resume crawling!");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Added by Amin for diverse crawling
	 * This is to notify a crawler to resume if its state is diverse.
	 */
	void resumeCrawling(){
		try {
			synchronized(this){
				pauseFlag = false;
				System.out.println(name);
				LOGGER.info("Notified " + this.name + " to continue crawling!");
				notify();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * A new state has been found!
	 *
	 * @param orrigionalState
	 *            the current state
	 * @return true if crawling must continue false otherwise.
	 * @throws CrawljaxException
	 */
	private boolean newStateDetected(StateVertix orrigionalState) throws CrawljaxException {

		/**
		 * An event has been fired so we are one level deeper
		 */

		depth++;
		LOGGER.info("RECURSIVE Call crawl; Current DEPTH= " + depth);
		if (!this.crawl()) {
			// Crawling has stopped
			controller.terminate(false);
			return false;
		}

		this.getStateMachine().changeState(orrigionalState);
		return true;
	}

	/**
	 * Initialize the Crawler, retrieve a Browser and go to the initial URL when no browser was
	 * present. rewind the state machine and goBack to the state if there is exactEventPath is
	 * specified.
	 *
	 * @throws InterruptedException
	 *             when the request for a browser is interrupted.
	 */
	public void init() throws InterruptedException {
		// Start a new CrawlPath for this Crawler
//        if (controller.getSession() != null) {
//            controller.getSession().startNewPath();
//        }

		this.browser = this.getBrowser();
		if (this.browser == null) {
			/**
			 * As the browser is null, request one and got to the initial URL, if the browser is
			 * Already set the browser will be in the initial URL.
			 */
//			this.browser = controller.getBrowserPool().requestBrowser();
            this.browser = controller.getBrowserPool().getCurrentBrowser();
//			LOGGER.info("Reloading page for navigating back");
			this.goToInitialURL();
		}
		// TODO Stefan ideally this should be placed in the constructor
		this.formHandler =
		        new FormHandler(getBrowser(), configurationReader.getInputSpecification(),
		                configurationReader.getCrawlSpecificationReader().getRandomInputInForms());

		this.candidateExtractor =
		        new CandidateElementExtractor(controller.getElementChecker(), this.getBrowser(),
		                formHandler, configurationReader.getCrawlSpecificationReader());
		/**
		 * go back into the previous state.
		 */
		try {
			this.goBackExact();
		} catch (CrawljaxException e) {
			LOGGER.error("Failed to backtrack", e);
		}
	}

	/**
	 * Terminate and clean up this Crawler, release the acquired browser. Notice that other Crawlers
	 * might still be active. So this function does NOT shutdown all Crawlers active that should be
	 * done with {@link CrawlerExecutor#shutdown()}
	 */
	public void shutdown() {
		controller.getBrowserPool().freeBrowser(this.getBrowser());
	}

	/**
	 * The main function stated by the ExecutorService. Crawlers add themselves to the list by
	 * calling {@link CrawlQueueManager#addWorkToQueue(Crawler)}. When the ExecutorService finds a
	 * free thread this method is called and when this method ends the Thread is released again and
	 * a new Thread is started
	 *
	 * @see java.util.concurrent.Executors#newFixedThreadPool(int)
	 * @see java.util.concurrent.ExecutorService
	 */
	@Override
	public void run() {
		if (!checkConstraints()) {
			// Constrains are not met at start of this Crawler, so stop immediately
			return;
		}
		// 如果backTrackPath不为空，说明这个Crawler是被注册的（已经在使用）
		if (backTrackPath.last() != null) {
			try {
				// 检查当前状态的crawler在StateVertex中是否有注册的任务,
				// 如果有就加入workInProgressCandidateActions中,并返回true
				// 如果没有直接return
				// 要知道backTrackPath不为空的crawler肯定是注册过任务的,那么什么情况下注册任务没了呢,
				// 就是pollCandidateCrawlAction()方法中一个crawler任务被偷掉的时候
				if (!backTrackPath.last().getTargetStateVertix().startWorking(this)) {
					return;
				}
			} catch (CrawljaxException e) {
				LOGGER.error("Received Crawljax exception", e);
			}
		}
		try {
			/**
			 * Init the Crawler
			 */
			try {
				this.init();
			} catch (InterruptedException e) {
				if (this.getBrowser() == null) {
					return;
				}
			}

			/**
			 * Hand over the main crawling
			 */
			// Amin: set strategicCrawl to true for enabling strategies for crawling
			//strategicCrawl = true;

			if (strategicCrawl){
				if (!this.guidedCrawl()) {
					controller.terminate(false);
				}
			}else{
				//this is the default Crawljax in DFS fashion
				if (!this.crawl()) {
					controller.terminate(false);
				}
			}

			/**
			 * Crawling is done; so the crawlPath of the current branch is known
			 */
			if (!fired) {
				controller.getSession().removeCrawlPath();
			}
		} catch (BrowserConnectionException e) {
			// The connection of the browser has gone down, most of the times it means that the
			// browser process has crashed.
			LOGGER.error("Crawler failed because the used browser died during Crawling",
			        new CrawlPathToException("Crawler failed due to browser crash", controller
			                .getSession().getCurrentCrawlPath(), e));
			// removeBrowser will throw a RuntimeException if the current browser is the last
			// browser in the pool.
			this.controller.getBrowserPool().removeBrowser(this.getBrowser(),
			        this.controller.getCrawlQueueManager());
			return;
		} catch (CrawljaxException e) {
			LOGGER.error("Crawl failed!", e);
		}
		/**
		 * At last failure or non shutdown the Crawler.
		 */
		this.shutdown();
	}

	/**
	 * Return the browser used in this Crawler Thread.
	 *
	 * @return the browser used in this Crawler Thread
	 */
	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * @return the state machine.
	 */
	public StateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * Test to see if the (new) DOM is changed with regards to the old DOM. This method is Thread
	 * safe.
	 *
	 * @param stateBefore
	 *            the state before the event.
	 * @param stateAfter
	 *            the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 */
	private boolean isDomChanged(final StateVertix stateBefore, final StateVertix stateAfter) {
		boolean isChanged = false;

		// do not need Oracle Comparators now, because hash of stripped dom is
		// already calculated
		// isChanged = !stateComparator.compare(stateBefore.getDom(),
		// stateAfter.getDom(), browser);
		isChanged = !stateAfter.equals(stateBefore);
		if (isChanged) {
			LOGGER.info("Dom is Changed!");
		} else {
			LOGGER.info("Dom Not Changed!");
		}

		return isChanged;
	}

	/**
	 * Checks the state and time constraints. This function is nearly Thread-safe.
	 *
	 * @return true if all conditions are met.
	 */
	private boolean checkConstraints() {
		long timePassed = System.currentTimeMillis() - controller.getSession().getStartTime();
		int maxCrawlTime = configurationReader.getCrawlSpecificationReader().getMaximumRunTime();
//		if ((maxCrawlTime != 0) && (timePassed > maxCrawlTime * ONE_SECOND)) {
//
//			LOGGER.info("Max time " + maxCrawlTime + " seconds passed!");
//			/* stop crawling */
//			return false;
//		}
		StateFlowGraph graph = controller.getSession().getStateFlowGraph();
		int maxNumberOfStates =
		        configurationReader.getCrawlSpecificationReader().getMaxNumberOfStates();
		if ((maxNumberOfStates != 0) && (graph.getAllStates().size() >= maxNumberOfStates)) {
			LOGGER.info("Max number of states " + maxNumberOfStates + " reached!");
			/* stop crawling */
			return false;
		}
		/* continue crawling */
		return true;
	}

	/**
	 * Amin
	 * Intelligent crawling through the clickables.
	 *
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	// 引导事件的触发
	private boolean guidedCrawl() throws CrawljaxException {
		StateVertix orrigionalState = this.getStateMachine().getCurrentState();

		while (true) {
			// 先检查使用何种策略，以及关键词的位置
			if (CrawljaxController.checkKSG() && !checkKeywordsPos()) {
				LOGGER.info("KSG算法结束");
				CrawljaxController.crawlStrategy = CrawlStrategy.BFS;
			}
			//TODO: should check depth...
			if (depthLimitReached(depth)) {
				return true;
			}

			if (!checkConstraints()) {
				return false;
			}

			/* 2020-1-4 */
			ClickResult result = null;
			boolean stateAdded = false;
			boolean clickForPrePos = false;
			/* 2020-1-4 */

			// Store the currentState to be able to 'back-track' later.
			LOGGER.info("orrigionalState is " + orrigionalState.getName());

			if (orrigionalState.searchForCandidateElements(candidateExtractor,
					configurationReader.getTagElements(), configurationReader.getExcludeTagElements(),
					configurationReader.getCrawlSpecificationReader().getClickOnce(),
					controller.getSession().getStateFlowGraph(), controller.isEfficientCrawling(), controller.isRandomEventExec(), false)) {
				// Only execute the preStateCrawlingPlugins when it's the first time
//				LOGGER.info("Starting preStateCrawlingPlugins...");

				List<CandidateElement> candidateElements = orrigionalState.getUnprocessedCandidateElements();

				//LOGGER.info("INNER # candidateElements for state " + orrigionalState.getName() + " is " + candidateElements.size());

				CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(), candidateElements);
				// update crawlActions
				orrigionalState.filterCandidateActions(candidateElements);

				// Amin: This is the count of candidates after filtering...
				CrawljaxController.NumCandidateClickables += orrigionalState.getNumCandidateElements();

				handlePreClick(orrigionalState);
			} else {
				//LOGGER.info("Outer # candidateElements for state " + orrigionalState.getName() + " is ZERO!");
				// check if there were not candidateElements for the current state (i.e., is leaf node)
				if (orrigionalState.isFullyExpanded()) {
					this.controller.getSession().getStateFlowGraph().removeFromNotFullExpandedStates(orrigionalState);
					/* 2020-1-3 */
					// 栈顶状态已经探测完备
					if (CrawljaxController.checkKSG() && crawlStack.peek() == orrigionalState) {
						// 为什么pop，因为没有找到想要的下一个状态
						crawlStack.pop();
						if (!tryCrawlStack.isEmpty() && tryCrawlStack.peek() == orrigionalState) {
							tryCrawlStack.pop();
						}
						LOGGER.info("KSG " + "状态 " + orrigionalState.getName() + " 已探测完备，从stack中移除");
					}
					/* 2020-1-3 */
				} else {
					/* 2020-1-3 */
					if (CrawljaxController.checkKSG()) {
						if (needSort(orrigionalState)) {
							sortCrawlAction(orrigionalState);
						} else {
							LOGGER.info("当前状态 " + orrigionalState.getName() + " 无需排序 " + "当前关键词序列 " + keywordPos);
						}
					}
					/* 2020-1-3 */
					CandidateCrawlAction action =
							orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);

					/* 2020-1-4 */
					LOGGER.info("正在触发事件 " + action);
					clickForPrePos = action.isClickForPrePos();
					/* 2020-1-4 */

					result = this.crawlAction(action);


					orrigionalState.finishedWorking(this, action);

					switch (result) {
						case newState:
							LOGGER.info("backTrackPath for new state " + this.getStateMachine().getCurrentState().getName() + " is " + backTrackPath);
							this.getStateMachine().getCurrentState().setCrawlPathToState(controller.getSession().getCurrentCrawlPath());

							/* 2020-1-4 */
							if (CrawljaxController.checkKSG()) {
								crawlStack.push(this.getStateMachine().getCurrentState());
								tryCrawlStack.push(this.getStateMachine().getCurrentState());
								stateAdded = true;
							}
							/* 2020-1-4 */

							System.out.println("newState");
							break;
						case cloneDetected:
							/* 2020-1-4 */
							if (CrawljaxController.checkKSG() && !crawlStack.contains(this.getStateMachine().getCurrentState())) {
								crawlStack.push(this.getStateMachine().getCurrentState());
								tryCrawlStack.push(this.getStateMachine().getCurrentState());
								stateAdded = true;
							}
							/* 2020-1-4 */
							System.out.println("cloneDetected");
							break;
						default:
							System.out.println("noChange");
							break;
					}
				}
			}

			StateVertix currentState = this.getStateMachine().getCurrentState();

			StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
			ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();

			// setting the crawl strategy
			CrawlStrategy strategy = CrawljaxController.crawlStrategy;

			// choose next state to crawl based on the strategy
			StateVertix nextToCrawl = nextStateToCrawl(strategy, result, stateAdded, clickForPrePos, new ArrayList<Eventable>());

			if (nextToCrawl == null) {
				LOGGER.info("nextToCrawl is null...");
				return false;
			}

			LOGGER.info("Next state to crawl is: " + nextToCrawl.getName());

			if (!nextToCrawl.equals(currentState)) {
				LOGGER.info("changing original from " + currentState.getName());
				this.getStateMachine().changeToNewState(nextToCrawl);
				LOGGER.info(" to " + this.getStateMachine().getCurrentState().getName());

				// start a new CrawlPath for this Crawler;
				controller.getSession().startNewPath();
				LOGGER.info("Reloading page for navigating back");
				reloadToSate(nextToCrawl);
			}
			orrigionalState = this.getStateMachine().getCurrentState();
		}
	}

	/**
	 * 算法3.4 SearchForElement
	 *
	 */
	public CandidateElement guidedCrawlForTargetElement(PlainElement targetElement, int keywordPos, List<Eventable> clickPath) throws CrawljaxException {
		this.setKeywordPos(keywordPos);
		controller.getSession().clearAlreadyCheckedStates();
		CandidateElement foundElement = searchForTargetElementOnState(targetElement, keywordPos);
		if (foundElement != null) {
			return foundElement;
		}
//		else {
//			if (!targetElement.getSeleniumAction().equalsIgnoreCase("click")) return null;
//		}

		StateVertix orrigionalState = this.getStateMachine().getCurrentState();
//		controller.getSession().addToAlreadyCheckedStates(orrigionalState);
		CrawljaxController.setCrawlStrategy(CrawlStrategy.KSG);

		while (true) {
			// 先检查使用何种策略，以及关键词的位置
			if (CrawljaxController.checkKSG() && !checkKeywordsPos()) {
				LOGGER.info("KSG算法结束");
				CrawljaxController.crawlStrategy = CrawlStrategy.BFS;
			} else {
			    // 初始化crawlStack
				if (crawlStack.size() == 0) {
					crawlStack.push(orrigionalState);
//					tryCrawlStack.push(orrigionalState);
				}
            }
			//TODO: should check depth...
			if (depthLimitReached(depth)) {
				return null;
			}

			if (!checkConstraints()) {
				return null;
			}

			/* 2020-1-4 */
			ClickResult result = null;
			boolean stateAdded = false;
			boolean clickForPrePos = false;
			/* 2020-1-4 */

			// Store the currentState to be able to 'back-track' later.
			LOGGER.info("orrigionalState is " + orrigionalState.getName());

			if (orrigionalState.searchForCandidateElements(candidateExtractor,
					configurationReader.getTagElements(), configurationReader.getExcludeTagElements(),
					configurationReader.getCrawlSpecificationReader().getClickOnce(),
					controller.getSession().getStateFlowGraph(), controller.isEfficientCrawling(), controller.isRandomEventExec(), false)) {
				// Only execute the preStateCrawlingPlugins when it's the first time
//				LOGGER.info("Starting preStateCrawlingPlugins...");

				List<CandidateElement> candidateElements = orrigionalState.getUnprocessedCandidateElements();

				//LOGGER.info("INNER # candidateElements for state " + orrigionalState.getName() + " is " + candidateElements.size());

				CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(), candidateElements);
				// update crawlActions
				orrigionalState.filterCandidateActions(candidateElements);

				// Amin: This is the count of candidates after filtering...
				CrawljaxController.NumCandidateClickables += orrigionalState.getNumCandidateElements();

				handlePreClick(orrigionalState);
			} else {
				//LOGGER.info("Outer # candidateElements for state " + orrigionalState.getName() + " is ZERO!");
				// check if there were not candidateElements for the current state (i.e., is leaf node)
				if (orrigionalState.isFullyExpanded()) {
					this.controller.getSession().getStateFlowGraph().removeFromNotFullExpandedStates(orrigionalState);
					/* 2020-1-3 */
					// 栈顶状态已经探测完备
					if (CrawljaxController.checkKSG() && crawlStack.peek() == orrigionalState) {
						// 为什么pop，因为没有找到想要的下一个状态
						crawlStack.pop();
						if (clickPath.size() > 0) clickPath.remove(clickPath.size() - 1 );
						if (!tryCrawlStack.isEmpty() && tryCrawlStack.peek() == orrigionalState) {
							tryCrawlStack.pop();
						}
						LOGGER.info("KSG " + "状态 " + orrigionalState.getName() + " 已探测完备，从stack中移除");
					}
					/* 2020-1-3 */
				} else {
					/* 2020-1-3 */
					if (CrawljaxController.checkKSG()) {
						if (needSort(orrigionalState)) {
							sortCrawlAction(orrigionalState);
						} else {
							LOGGER.info("当前状态 " + orrigionalState.getName() + " 无需排序 " + "当前关键词序列 " + keywordPos);
						}
					}
					/* 2020-1-3 */
					CandidateCrawlAction action =
							orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);

					/* 2020-1-4 */
					LOGGER.info("正在触发事件 " + action);
					clickForPrePos = action.isClickForPrePos();
					/* 2020-1-4 */

					result = this.crawlAction(action);
					controller.getSession().fireEventNum++;


					orrigionalState.finishedWorking(this, action);

					Eventable fireEvent = new Eventable(action.getCandidateElement(), action.getEventType());
					switch (result) {
						case newState:
							controller.getSession().crawlStateNum++;
							LOGGER.info("backTrackPath for new state " + this.getStateMachine().getCurrentState().getName() + " is " + backTrackPath);
							if (this.getStateMachine().getCurrentState().getCrawlPathToState().size() == 0) {
								this.getStateMachine().getCurrentState().setCrawlPathToState(controller.getSession().getCurrentCrawlPath());
							}

							/* 2020-1-4 */
							if (CrawljaxController.checkKSG()) {
								crawlStack.push(this.getStateMachine().getCurrentState());
								tryCrawlStack.push(this.getStateMachine().getCurrentState());
								stateAdded = true;
							}
							clickPath.add(fireEvent);
							/* 2020-1-4 */

							System.out.println("newState");

							// 搜索目标元素
							foundElement = searchForTargetElementOnState(targetElement, keywordPos);
							if (foundElement != null) {
								return foundElement;
							}
							break;
						case cloneDetected:
							/* 2020-1-4 */
							if (CrawljaxController.checkKSG() && !crawlStack.contains(this.getStateMachine().getCurrentState())) {
								crawlStack.push(this.getStateMachine().getCurrentState());
								tryCrawlStack.push(this.getStateMachine().getCurrentState());
								stateAdded = true;
							}
							clickPath.add(fireEvent);
							/* 2020-1-4 */
							System.out.println("cloneDetected");

							if (!controller.getSession().getAlreadyCheckedStates().contains(this.getStateMachine().getCurrentState())) {
								foundElement = searchForTargetElementOnState(targetElement, keywordPos);
								if (foundElement != null) {
									return foundElement;
								}
//								controller.getSession().addToAlreadyCheckedStates(this.getStateMachine().getCurrentState());
							}
							break;
						default:
							System.out.println("noChange");
							break;
					}
				}
			}

			StateVertix currentState = this.getStateMachine().getCurrentState();

			// setting the crawl strategy
			CrawlStrategy strategy = CrawljaxController.crawlStrategy;

			// choose next state to crawl based on the strategy
			StateVertix nextToCrawl = nextStateToCrawl(strategy, result, stateAdded, clickForPrePos, clickPath);

			if (nextToCrawl == null) {
				LOGGER.info("Something is wrong! nextToCrawl is null...");
				return null;
			}

			LOGGER.info("Next state to crawl is: " + nextToCrawl.getName());

			if (!nextToCrawl.equals(currentState)) {
				LOGGER.info("changing original from " + currentState.getName());
				this.getStateMachine().changeToNewState(nextToCrawl);
				LOGGER.info(" to " + this.getStateMachine().getCurrentState().getName());

				// start a new CrawlPath for this Crawler;
				controller.getSession().startNewPath();
				LOGGER.info("Reloading page for navigating back");
				reloadToSate(nextToCrawl);
			}
			orrigionalState = this.getStateMachine().getCurrentState();
		}
	}

	public CandidateElement searchForTargetElementOnState(PlainElement targetElement, int keywordPos) {
		if (StringUtils.isNotEmpty(targetElement.getTagName())) {
			StateVertix stateVertix = this.stateMachine.getCurrentState();
			controller.getSession().addToAlreadyCheckedStates(stateVertix);
			// 根据元素标签或者属性查找
			Set<WebElement> result = new HashSet<>();
			List<WebElement> tempResult;
			// 根据tag查找
			tempResult = browser.getWebElements(new Identification(Identification.How.tag, targetElement.getTagName()));
			if (tempResult != null) result.addAll(tempResult);
			tempResult = browser.getWebElements(new Identification(Identification.How.tag, "a"));
			if (tempResult != null) result.addAll(tempResult);
			// 根据属性查找
			if (StringUtils.isNotEmpty(targetElement.getNameAttribute())) {
				tempResult = browser.getWebElements(new Identification(Identification.How.name, targetElement.getNameAttribute()));
				if (tempResult != null) result.addAll(tempResult);
			}
			if (StringUtils.isNotEmpty(targetElement.getClassAttribute())) {
				try {
					tempResult = browser.getWebElements(new Identification(Identification.How.clazz, targetElement.getClassAttribute()));
					if (tempResult != null) result.addAll(tempResult);
				} catch (Exception e) {

				}
			}
			if (StringUtils.isNotEmpty(targetElement.getId())) {
				tempResult = browser.getWebElements(new Identification(Identification.How.id, targetElement.getId()));
				if (tempResult != null) result.addAll(tempResult);
			}
			if (StringUtils.isNotEmpty(targetElement.getText())) {
				tempResult = browser.getWebElements(new Identification(Identification.How.partialText, targetElement.getText()));
				if (tempResult != null) result.addAll(tempResult);
			}

//			List<TagElement> tagElements = new ArrayList<>();
//			tagElements.add(new TagElement(targetElement.getTagName()));
//			List<CandidateElement> result = stateVertix.searchForTargetElements(candidateExtractor, tagElements, configurationReader.getExcludeTagElements(),
//			configurationReader.getCrawlSpecificationReader().getClickOnce());
			// 检查相似度
			double maxSimScore = 0.0;
			WebElement mostSimElement = null;
			for (WebElement ele : result) {
				if (!ele.isDisplayed()) continue;
				// 结构相似度
				double sim1 = SimHelper.getSimilarityOfStructure(ele, targetElement, browser);
				// 语义相似度
				double sim2 = SimHelper.getSimilarityOfSemantic(ele, targetElement, browser);
				double beta = 0.4;
				double score = sim1 * beta + sim2 * (1-beta);

				if (score>=0.5 && score>maxSimScore && ele.isDisplayed()) {
					maxSimScore = score;
					mostSimElement = ele;

//					System.out.println("Similarity of structure: "+sim1);
//					System.out.println("Similarity of semantics: "+sim2);
//					System.out.println("Comprehensive similarity: "+maxSimScore);
				}
			}
			if (mostSimElement != null) {
				Document dom = null;
				try {
					dom = Helper.getDocument(browser.getDom());
					String xpath = SimHelper.getElementXPath(browser, mostSimElement);
					NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, xpath.toUpperCase());
					if (nodeList.getLength() > 1) {
						return null;
					}
					Element element = (Element) nodeList.item(0);
					CandidateElement candidateElement = new CandidateElement(element, xpath);
					return  candidateElement;
				} catch (SAXException | IOException | XPathExpressionException e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			LOGGER.error("no element tag founded...");
		}
		return  null;
	}

	/**
	 * 每次选择事件前都要进行排序，因为同一个状态在不同的试探环节中可能对应着不同的目标关键词序列下标
	 * Crawler在前往下一个状态时，当前状态剩余未触发的事件会被注册到新建的crawler，并把这些crawler提交到线程池
	 * 因此在对状态事件进行排序时，需要判断状态事件是否已被注册，如已注册就需要对#StateVertix#registeredCrawlers进行排序
	 * 以供StateVertix#pollCandidateCrawlAction方法进行任务窃取
	 * 此外，初始crawler结束，也就代表着KSG算法结束，进入DFS阶段，剩余的状态及事件会提交到线程池的其他crawler执行，这些事件无需排序
	 * @param state
	 */
	private void sortCrawlAction(StateVertix state) {
		if (state == null) {
			return;
		}
		if (state.getCandidateActions().size() != 0) {
			List<CandidateCrawlAction> crawlActionList = new ArrayList<>(state.getCandidateActions());
			Collections.sort(crawlActionList, (o1, o2)->{return getDiff(o1, o2);});
			state.setCandidateActions(new LinkedBlockingDeque<>(crawlActionList));
		} else if (state.getRegisteredCrawlers().size() != 0) {
			List<Crawler> crawlerList = new ArrayList<>(state.getRegisteredCrawlers());
			final ConcurrentHashMap<Crawler, CandidateCrawlAction> registeredCandidateActions = state.getRegisterdCandidateActions();
			Collections.sort(crawlerList, (Comparator<Crawler>) (c1, c2)->{
					CandidateCrawlAction o1 = registeredCandidateActions.get(c1);
					CandidateCrawlAction o2 = registeredCandidateActions.get(c2);
					return getDiff(o1, o2);
			});
			state.setRegisteredCrawlers(new LinkedBlockingDeque<>(crawlerList));
		}
		LOGGER.info("可点击事件已排序，当前关键词序列 " + keywordPos);
		state.setSortSnapshot(state.new SortSnapshot(tryCrawlStack.isEmpty(), keywordPos));
	}

	private int getDiff(CandidateCrawlAction o1, CandidateCrawlAction o2) {
		double diff = 0;
		if (tryCrawlStack.isEmpty()) {
			double s1 = o1.getIntervalSimilarScore(keywordPos);
			if (Double.isNaN(s1) || Double.isInfinite(s1)) s1 = 0.0;
			double s2 = o2.getIntervalSimilarScore(keywordPos);
			if (Double.isNaN(s2) || Double.isInfinite(s2)) s2 = 0.0;
			diff = s1 - s2;
		} else {
			double s1 = o1.getSimilarScore(keywordPos);
			if (Double.isNaN(s1) || Double.isInfinite(s1)) s1 = 0.0;
			double s2 = o2.getSimilarScore(keywordPos);
			if (Double.isNaN(s2) || Double.isInfinite(s2)) s2 = 0.0;
			diff = s1 - s2;
		}
		// 倒序，从大到小，diff为double，直接返回int可能会丢失精度
//		return -new BigDecimal(diff).compareTo(new BigDecimal(0.0));
		return diff < 0 ? 1 : (diff > 0 ? -1 : 0);
	}

	private boolean needSort(StateVertix state) {
		StateVertix.SortSnapshot sortSnapshot = state.getSortSnapshot();
		if (sortSnapshot == null) {
			return true;
		}
		if (sortSnapshot.emptyStack != tryCrawlStack.isEmpty()) {
			return true;
		}
		if (sortSnapshot.keywordPos != this.keywordPos) {
			return true;
		}
		return false;
	}

	/**
	 * 对状态中有先后关系约束的事件进行合并整理
	 * @param state
	 */
	private void handlePreClick(StateVertix state) {
		if (state.getCandidateActions().size() == 0) {
			return;
		}
		Map<CandidateCrawlAction, TagElement> actionTagMap = new HashMap<CandidateCrawlAction, TagElement>();
		for (CandidateCrawlAction crawlAction : state.getCandidateActions()) {
			for (TagElement tagElement : CrawljaxController.preClickConstraintMap.keySet()) {
				if (actionMathcTag(crawlAction, tagElement)) {
					actionTagMap.put(crawlAction, tagElement);
				}
			}
		}
		Set<CandidateCrawlAction> waitToDelete = new HashSet<CandidateCrawlAction>();
		for (CandidateCrawlAction crawlAction: actionTagMap.keySet()) {
			List<TagElement> tagElementList = CrawljaxController.preClickConstraintMap.get(actionTagMap.get(crawlAction));
			for (CandidateCrawlAction preClickAction: state.getCandidateActions()) {
				if (actionTagMap.keySet().contains(preClickAction)) {
					continue;
				}
				for (TagElement tagElement: tagElementList) {
					if (actionMathcTag(preClickAction, tagElement)) {
						crawlAction.getPreClickActionList().add(preClickAction);
						waitToDelete.add(preClickAction);
						break;
					}
				}
			}
		}
		for (CandidateCrawlAction delete: waitToDelete) {
			state.getCandidateActions().remove(delete);
			state.decreaseCandidateElements();
			CrawljaxController.NumCandidateClickables--;
		}
	}

	/**
	 * 检查一个CandidateCrawlAction和TagElement是否匹配
	 * @param crawlAction
	 * @param tagElement
	 * @return
	 */
	private boolean actionMathcTag(CandidateCrawlAction crawlAction, TagElement tagElement) {
		Element element = crawlAction.getCandidateElement().getElement();
		if (element == null) {
			return false;
		}
		if (!StringUtils.equalsIgnoreCase(element.getNodeName(), tagElement.getName())) {
			return false;
		}
		for (TagAttribute tagAttribute: tagElement.getAttributes()) {
			if (!StringUtils.equalsIgnoreCase(tagAttribute.getValue(), element.getAttribute(tagAttribute.getName()))) {
				return false;
			}
		}
		return true;
	}

	private boolean checkKeywordsPos() {
		return keywordPos >= 0 && keywordPos < CrawljaxController.keywords.size();
	}

	/**
	 * Amin
	 * Reload the browser to the given state.
	 *
	 * @throws CrawljaxException
	 *             if the {@link Eventable#getTargetStateVertix()} encounters an error.
	 */
	private void reloadToSate(StateVertix s) throws CrawljaxException {
		/**
		 * Thread safe
		 */
		this.goToInitialURL();
		StateVertix curState = controller.getSession().getInitialState();

		LOGGER.info("reloading to state " + s.getName());

		if (s.equals(curState)){   // no Eventable to execute for the index state
			LOGGER.info("reloaded to index! no execution needed!!!");
			return;
		}

		LOGGER.info("crawlpath to state " + s.getName() + " is  " + s.getCrawlPathToState());

		for (Eventable clickable : s.getCrawlPathToState()) {

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}

			LOGGER.info("Backtracking by executing " + clickable.getEventType() + " on element: "
			        + clickable);

			this.getStateMachine().changeState(clickable.getTargetStateVertix());

			curState = clickable.getTargetStateVertix();

			controller.getSession().addEventableToCrawlPath(clickable);

			this.handleInputElements(clickable);

			if (this.fireEvent(clickable)) {
				depth++;

				/**
				 * Run the onRevisitStateValidator(s)
				 */
				CrawljaxPluginsUtil.runOnRevisitStatePlugins(this.controller.getSession(),
				        curState);
			}

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}
		}
	}



	/**
	 * Amin
	 * Find which state to crawl next
	 * 可以返回null
	 */
	public StateVertix nextStateToCrawl(CrawlStrategy strategy, ClickResult result, boolean stateAdded, boolean clickForPrePos, List<Eventable> clickPath){
		int index = 0;
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();

		switch (strategy){
			case DFS:
				// continue with the last-in state
				if (notFullExpandedStates.size()>0)
					index = notFullExpandedStates.size()-1;
				break;
			case BFS:
				// next state is the first-in state
				index = 0;
				break;
			case Rand:
				Random randomGenerator = new Random();
				if (notFullExpandedStates.size()>0)
					index = randomGenerator.nextInt(notFullExpandedStates.size());
				break;
			case Div:
				if (notFullExpandedStates.size()>0){
					//index = nextForDiverseCrawlingCovOnly();
					//index = nextForDiverseCrawlingDDOnly();
					//index = nextForDiverseCrawlingPDOnly();

					//index = nextForDiverseCrawlingCo_PD(1.0, 1.0);
					//index = nextForDiverseCrawlingCo_DD(1.0, 1.0);
					//index = nextForDiverseCrawling(1.0, 1.0);  // PD+DD

					index = nextForDiverseCrawling3(1.0,1.0,1.0); // all
				}
				break;
			case KSG:
				if (notFullExpandedStates.size() > 0) {
					return nextStateToCrawlForKSG(result, stateAdded, clickForPrePos, clickPath);
				}
				break;
			default:
				break;
		}

		// Amin: TODO: Check for the index
		if (index==-1)
			return null;

		if (notFullExpandedStates.size()==0)
			return null;

		LOGGER.info("Satet " + notFullExpandedStates.get(index) + " selected as the nextStateToCrawl");

		return notFullExpandedStates.get(index);
	}

	/**
	 * KSG算法，下一个待测状态
	 * @param result
	 * @param stateAdded
	 * @param clickForPrePos
	 * @return
	 */
	private StateVertix nextStateToCrawlForKSG(ClickResult result, boolean stateAdded, boolean clickForPrePos, List<Eventable> clickPath) {
//		LOGGER.info("KSG starting...");
		if (crawlStack.isEmpty()) {
//			LOGGER.info("栈已空，ksg算法探测结束");
			// 从未完全探测状态中拿出一个状态继续探测
			LOGGER.info("转为BFS算法");
			CrawljaxController.setCrawlStrategy(CrawlStrategy.BFS);
			// TODO: 2020/2/29
			StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
			ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
			return (notFullExpandedStates == null || notFullExpandedStates.size() == 0) ? null : notFullExpandedStates.get(0);
		}
		if (result == null) {
			LOGGER.info("没有事件被触发，继续探测状态 " + crawlStack.peek().getName());
			return crawlStack.peek();
		}
		if (clickForPrePos) {
			LOGGER.info("当前触发事件的目标为上一个关键词序列 " + (keywordPos-1));
			if (stateAdded) {
				double sim = crawlStack.peek().getSimilarScore(keywordPos-1);
				LOGGER.info("当前状态 " + crawlStack.peek().getName() + " 和关键词序列 " + (keywordPos-1) + " 的语义相似度为 " + sim);
				if (sim > Threshold.T1.getValue()) {
					tryCrawlStack.clear();
					LOGGER.info("继续探测当前状态，目标关键词序列 " + keywordPos);
				} else {
					tryCrawlStack.clear();
					crawlStack.pop();
					if (clickPath.size() > 0) clickPath.remove(clickPath.size()-1);
					LOGGER.info("回退到上一状态 " + crawlStack.peek() + " 目标关键词序列 " + keywordPos);
				}
			} else {
				LOGGER.info("没有状态入栈，继续探测当前状态，目标关键词序列" + keywordPos);
			}
			return crawlStack.peek();
		}
		switch (result) {
			case newState:
				LOGGER.info("试探栈中加入了新状态 " + crawlStack.peek().getName() + "正在进行试探...");
				return doTry(clickPath);
			case cloneDetected:
				if (stateAdded) {
					LOGGER.info("试探栈中加入了新状态 " + crawlStack.peek().getName() + "正在进行试探...");
					return doTry(clickPath);
				} else {
					LOGGER.info("避免产回路，继续探测前一状态 " + crawlStack.peek().getName());
					return crawlStack.peek();
				}
			case domUnChanged:
				LOGGER.info("触发事件后状态未发生改变，继续探测当前状态 " + crawlStack.peek().getName());
				return crawlStack.peek();
			default:
				return crawlStack.peek();
		}
	}

	/**
	 * 当且仅当tryCrawlStack中加入了状态，才进行试探
	 * @return
	 */
	private StateVertix doTry(List<Eventable> clickPath) {
		double sim = crawlStack.peek().getSimilarScore(keywordPos);
		LOGGER.info("当前状态 " + crawlStack.peek().getName() + " 和关键词序列 " + keywordPos + " 的语义相似度为 " + sim);
		LOGGER.info("当前试探次数 " + tryCrawlStack.size());
		if (sim >= Threshold.T1.getValue()) {
			double C = getTrySim();
			LOGGER.info("试探相似度=" + C);
			if (C >= Threshold.T2.getValue()) {
				LOGGER.info("关键词序列 " + keywordPos + " 已发现，正在前往下一个关键词...");
				keywordPos++;
				tryCrawlStack.clear();
				return null;
			} else if (tryCrawlStack.size() < 3) {
				LOGGER.info("继续试探当前关键词序列 " + keywordPos);
			} else {
				tryCrawlStack.pop();
				crawlStack.pop();
				if (clickPath.size() > 0) clickPath.remove(clickPath.size()-1);
				LOGGER.info("当前试探次数已达3次，进行回退，回退到 " + crawlStack.peek().getName());
			}
		} else {
			tryCrawlStack.pop();
			crawlStack.pop();
			if (clickPath.size() > 0) clickPath.remove(clickPath.size()-1);
			LOGGER.info("当前状态语义相似度未达到阈值T1，进行回退，回退到 " + crawlStack.peek().getName());
		}
		return crawlStack.peek();
	}

	/**
	 * 计算试探相似度
	 * @return
	 */
	private double getTrySim() {
		if (tryCrawlStack.size() == 0) {
			throw new RuntimeException("tryCrawlStack不能为空");
		}
		double K = 0;
		// 先加当前状态，再加剩余状态
		K += tryCrawlStack.peek().getSimilarScore(keywordPos);
		for (int i=0; i<tryCrawlStack.size()-1; i++) {
			K += Threshold.TRY_W.getValue() * tryCrawlStack.get(i).getSimilarScore(keywordPos);
		}

		return 1 / (1 + Math.pow(Math.E, -(K)));
	}

	/**
	 * Added by Amin
	 * This is a main method for diverse crawling which decides about which state to crawl next.
	 * It calculates pair-wise diversity for states of waiting crawlers
	 *
	 * 	 @param PD_weight
	 *            the user defined weight for path-diversity
	 *   @param DD_weight
	 *            the user defined weight for DOM-diversity
	 */
	public int nextForDiverseCrawling(double PD_weight, double DD_weight){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		ArrayList<Double> minPathDiv = new ArrayList<Double>();
		double minDD, AvgMinDD=0.0;

		ArrayList<Double> minDOMDiv = new ArrayList<Double>();
		double minPD=1.0, PD, AvgMinPD=0.0;

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){
			minDD = sfg.getMinDOMDiversity(notFullExpandedStates.get(i));
			//LOGGER.info("MinDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);

			for (int j=0; j < notFullExpandedStates.size(); j++){
				if (notFullExpandedStates.get(i)!=notFullExpandedStates.get(j)){
					PD = sfg.getPathDiversity(notFullExpandedStates.get(i), notFullExpandedStates.get(j));

					//LOGGER.info("PD of state " +  notFullExpandedStates.get(i).getName()
					//		+ " and state " +  notFullExpandedStates.get(j).getName() + " is " + PD);
					if (PD < minPD)
						minPD = PD;
				}
			}

			//LOGGER.info("minPD of state " +  notFullExpandedStates.get(i).getName() + " is " + minPD);
			minPathDiv.add(minPD);
			AvgMinPD += minPD;
			minPD=1.0;
			//LOGGER.info("minDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);
			minDOMDiv.add(minDD);
			AvgMinDD += minDD;
			minDD=1.0;
		}
		AvgMinPD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinPD is " +  AvgMinPD);
		AvgMinDD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinDD is " +  AvgMinDD);

		double winnerScore = 0.0;
		double stateScore = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			stateScore = PD_weight*minPathDiv.get(i) + DD_weight*minDOMDiv.get(i);
			//Amin: The idea is changed to select the one with the max score
			//if (stateScore >= AvgTotalDiv){
			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with diversity score "  + (PD_weight*minPathDiv.get(index) + DD_weight*minDOMDiv.get(index)));

		return index;
	}


	/**
	 * Added by Amin : coverage
	 */
	public int nextForDiverseCrawlingCovOnly(){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		if (notFullExpandedStates.size()==0)
			return -1;

		double coverageIncrease;

		double winnerCoverageIncrease = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			coverageIncrease = sfg.getCoverageIncrease(notFullExpandedStates.get(i));

			if (coverageIncrease >= winnerCoverageIncrease){
				winnerCoverageIncrease = coverageIncrease;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with coverageIncrease "  + winnerCoverageIncrease);

		return index;
	}

	/**
	 * Added by Amin
	 * Combining nextForDiverseCrawling2 and nextForDiverseCrawling2
	 *
	 * 	 @param PD_weight
	 *            the user defined weight for path-diversity
	 *   @param DD_weight
	 *            the user defined weight for DOM-diversity
	 */
	public int nextForDiverseCrawling3(double Cov_weight, double PD_weight, double DD_weight){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		ArrayList<Double> minPathDiv = new ArrayList<Double>();
		ArrayList<Double> minDOMDiv = new ArrayList<Double>();
		double minPD=1.0, PD, AvgMinPD=0.0;
		double minDD, AvgMinDD=0.0;

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){
			minDD = sfg.getMinDOMDiversity(notFullExpandedStates.get(i));
			//LOGGER.info("MinDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);

			for (int j=0; j < notFullExpandedStates.size(); j++){
				if (notFullExpandedStates.get(i)!=notFullExpandedStates.get(j)){
					PD = sfg.getPathDiversity(notFullExpandedStates.get(i), notFullExpandedStates.get(j));
					//LOGGER.info("PD of state " +  notFullExpandedStates.get(i).getName()
					//		+ " and state " +  notFullExpandedStates.get(j).getName() + " is " + PD);
					if (PD < minPD)
						minPD = PD;
				}
			}

			//LOGGER.info("minPD of state " +  notFullExpandedStates.get(i).getName() + " is " + minPD);
			minPathDiv.add(minPD);
			AvgMinPD += minPD;
			minPD=1.0;
			//LOGGER.info("minDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);
			minDOMDiv.add(minDD);
			AvgMinDD += minDD;
			minDD=1.0;
		}
		AvgMinPD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinPD is " +  AvgMinPD);
		AvgMinDD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinDD is " +  AvgMinDD);

		double winnerScore = 0.0;
		double stateScore = 0.0;
		double coverageIncrease = 0.0;
		double winnerCoverageIncrease = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			coverageIncrease = sfg.getCoverageIncrease(notFullExpandedStates.get(i));

			stateScore = Cov_weight*coverageIncrease + PD_weight*minPathDiv.get(i) + DD_weight*minDOMDiv.get(i);

			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				winnerCoverageIncrease = coverageIncrease;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with score "  + (Cov_weight*winnerCoverageIncrease + PD_weight*minPathDiv.get(index) + DD_weight*minDOMDiv.get(index)));

		return index;
	}


	/**
	 * Added by Amin
	 * This is a main method for diverse crawling which decides about which state to crawl next.
	 * It calculates pair-wise diversity for states of waiting crawlers
	 *
	 */
	public int nextForDiverseCrawlingDDOnly(){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		double minDD, AvgMinDD=0.0;

		ArrayList<Double> minDOMDiv = new ArrayList<Double>();

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){
			minDD = sfg.getMinDOMDiversity(notFullExpandedStates.get(i));
			//LOGGER.info("MinDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);

			minDOMDiv.add(minDD);
			AvgMinDD += minDD;
			minDD=1.0;
		}

		AvgMinDD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinDD is " +  AvgMinDD);
		// average total diversity score
		double winnerScore = 0.0;
		double stateScore = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			stateScore = minDOMDiv.get(i);
			//Amin: The idea is changed to select the one with the max score
			//if (stateScore >= AvgTotalDiv){
			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with DOM diversity score "  + minDOMDiv.get(index));

		return index;
	}



	/**
	 * Added by Amin
	 * This is a main method for diverse crawling which decides about which state to crawl next.
	 * It calculates pair-wise diversity for states of waiting crawlers
	 */
	public int nextForDiverseCrawlingPDOnly(){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		ArrayList<Double> minPathDiv = new ArrayList<Double>();
		double minPD=1.0, PD, AvgMinPD=0.0;

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){

			for (int j=0; j < notFullExpandedStates.size(); j++){
				if (notFullExpandedStates.get(i)!=notFullExpandedStates.get(j)){
					PD = sfg.getPathDiversity(notFullExpandedStates.get(i), notFullExpandedStates.get(j));

					//LOGGER.info("PD of state " +  notFullExpandedStates.get(i).getName()
					//		+ " and state " +  notFullExpandedStates.get(j).getName() + " is " + PD);
					if (PD < minPD)
						minPD = PD;
				}
			}

			//LOGGER.info("minPD of state " +  notFullExpandedStates.get(i).getName() + " is " + minPD);
			minPathDiv.add(minPD);
			AvgMinPD += minPD;
			minPD=1.0;

		}
		AvgMinPD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinPD is " +  AvgMinPD);

		double winnerScore = 0.0;
		double stateScore = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			stateScore = minPathDiv.get(i);
			//Amin: The idea is changed to select the one with the max score
			//if (stateScore >= AvgTotalDiv){
			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with PD diversity score "  + minPathDiv.get(index));

		return index;
	}



	/**
	 * Added by Amin
	 */
	public int nextForDiverseCrawlingCo_DD(double Cov_weight, double DD_weight){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		ArrayList<Double> minDOMDiv = new ArrayList<Double>();
		double minDD, AvgMinDD=0.0;

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){
			minDD = sfg.getMinDOMDiversity(notFullExpandedStates.get(i));
			//LOGGER.info("MinDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);
			minDOMDiv.add(minDD);
			AvgMinDD += minDD;
			minDD=1.0;
		}
		AvgMinDD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinDD is " +  AvgMinDD);

		double winnerScore = 0.0;
		double stateScore = 0.0;
		double coverageIncrease = 0.0;
		double winnerCoverageIncrease = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			coverageIncrease = sfg.getCoverageIncrease(notFullExpandedStates.get(i));

			stateScore = Cov_weight*coverageIncrease + DD_weight*minDOMDiv.get(i);

			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				winnerCoverageIncrease = coverageIncrease;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with score "  + (Cov_weight*winnerCoverageIncrease + DD_weight*minDOMDiv.get(index)));

		return index;
	}


	/**
	 * Added by Amin
	 */
	public int nextForDiverseCrawlingCo_PD(double Cov_weight, double PD_weight){
		StateFlowGraph sfg = controller.getSession().getStateFlowGraph();
		ArrayList<StateVertix> notFullExpandedStates = sfg.getNotFullExpandedStates();
		int index = 0;

		ArrayList<Double> minPathDiv = new ArrayList<Double>();
		double minPD=1.0, PD, AvgMinPD=0.0;

		// calculating minimum pair-wise Path-diversity and pair-wise DOM-diversity
		// Amin: TODO may need to check path-diversity w.r.t all states in the SFG
		for (int i=0; i < notFullExpandedStates.size(); i++){
			//LOGGER.info("MinDD of state " +  notFullExpandedStates.get(i).getName() + " is " + minDD);

			for (int j=0; j < notFullExpandedStates.size(); j++){
				if (notFullExpandedStates.get(i)!=notFullExpandedStates.get(j)){
					PD = sfg.getPathDiversity(notFullExpandedStates.get(i), notFullExpandedStates.get(j));
					//LOGGER.info("PD of state " +  notFullExpandedStates.get(i).getName()
					//		+ " and state " +  notFullExpandedStates.get(j).getName() + " is " + PD);
					if (PD < minPD)
						minPD = PD;
				}
			}

			//LOGGER.info("minPD of state " +  notFullExpandedStates.get(i).getName() + " is " + minPD);
			minPathDiv.add(minPD);
			AvgMinPD += minPD;
			minPD=1.0;
		}
		AvgMinPD /= notFullExpandedStates.size();
		//LOGGER.info("AvgMinPD is " +  AvgMinPD);

		double winnerScore = 0.0;
		double stateScore = 0.0;
		double coverageIncrease = 0.0;
		double winnerCoverageIncrease = 0.0;

		for (int i=0; i < notFullExpandedStates.size(); i++){
			coverageIncrease = sfg.getCoverageIncrease(notFullExpandedStates.get(i));

			stateScore = Cov_weight*coverageIncrease + PD_weight*minPathDiv.get(i);

			if (stateScore >= winnerScore){
				winnerScore = stateScore;
				winnerCoverageIncrease = coverageIncrease;
				index = i;
			}
		}

		LOGGER.info("The winner state is " +  notFullExpandedStates.get(index).getName()
				+ " with score "  + (Cov_weight*winnerCoverageIncrease + PD_weight*minPathDiv.get(index)));

		return index;
	}
}
