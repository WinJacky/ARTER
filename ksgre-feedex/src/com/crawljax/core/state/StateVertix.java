package com.crawljax.core.state;

import com.crawljax.core.*;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.Helper;
import com.crawljax.util.TreeEditDist.LblTree;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The state vertix class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertix it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 * 
 * @author mesbah
 * @author amin
 * @version $Id: StateVertix.java 435M 2012-05-29 19:11:04Z (local) $
 */
public class StateVertix {

	private static final long serialVersionUID = 123400017983488L;
	private static final Logger LOGGER = Logger.getLogger(StateVertix.class);
	private long id;
	private String name;
	private String dom;
	private final String strippedDom;

	// added by Amin to store DOM tree structure
	private LblTree domTree;

	/**
	 * The path followed from the index to this state.
	 */
	private final CrawlPath crawlPath;

	

	//Amin: used to store path to this state. Note that crawlPath stores path to the parent (source state) of this state
	private List<Eventable> crawlPathToState = new ArrayList<Eventable>();
	//Amin: 
	public List<Eventable> getCrawlPathToState() {
		return crawlPathToState;
	}
	//Amin: 
	public void setCrawlPathToState(CrawlPath cp) {
		for (Eventable e: cp)
			this.crawlPathToState.add(e);
		LOGGER.info("crawlpath to state " + this.getName() + " is set to " + this.crawlPathToState);
	}

	public void addEventableToState(Eventable e) {
		this.crawlPathToState.add(e);
	}
	
	
	private final String url;
	private boolean guidedCrawling = false;
	//public static List<CandidateElement> candidateElementChecker=new ArrayList<CandidateElement>();

	public LinkedBlockingDeque<CandidateCrawlAction> getCandidateActions() {
		return candidateActions;
	}

	/**
	 * This list is used to store the possible candidates. If it is null its not initialised if it's
	 * a empty list its empty.
	 */
	private LinkedBlockingDeque<CandidateCrawlAction> candidateActions;
	
	//Amin: stores number of remaining CandidateElements
	private int numCandidateElements = 0;

	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> registerdCandidateActions =
		new ConcurrentHashMap<Crawler, CandidateCrawlAction>();
	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> workInProgressCandidateActions =
		new ConcurrentHashMap<Crawler, CandidateCrawlAction>();

	private final Object candidateActionsSearchLock = new Object();

	private LinkedBlockingDeque<Crawler> registeredCrawlers =
		new LinkedBlockingDeque<Crawler>();

	// 关键文本内容用于比较状态和关键词的语义相似度(分词后的)
	private List<String> keyContent;

	// <关键词序列下标，语义相似度得分>
	private static Map<Integer, Double> similarScoreMap = new HashMap<Integer, Double>();

	public SortSnapshot getSortSnapshot() {
		return sortSnapshot;
	}

	public void setSortSnapshot(SortSnapshot sortSnapshot) {
		this.sortSnapshot = sortSnapshot;
	}

	private SortSnapshot sortSnapshot;

	/**
	 * 用于优化，减少不必要的可点击事件排序
	 * 记录排序时的现场信息
	 */
	public class SortSnapshot {
		// 排序时试探栈是否为空
		public boolean emptyStack;
		// 排序时的关键词序列下标
		public int keywordPos;

		public SortSnapshot() {}

		public SortSnapshot(boolean emptyStack, int keywordPos) {
			this.emptyStack = emptyStack;
			this.keywordPos = keywordPos;
		}
	}

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertix() {
		this.strippedDom = "";
		this.url = "";
		this.crawlPath = null;
	}

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	public StateVertix(String name, String dom, CrawlPath cPath) {
		this(null, name, dom, dom, cPath);
	}

	/**
	 * Defines a State.
	 * 
	 * @param url
	 *            the current url of the state
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 * @param strippedDom
	 *            the stripped dom by the OracleComparators
	 */
	// , CrawlPath cPath
	public StateVertix(String url, String name, String dom, String strippedDom, CrawlPath cPath) {
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
		this.crawlPath = cPath;

		try {
			Document doc = Helper.getDocument(this.dom);
			DocumentTraversal traversal = (DocumentTraversal) doc;
			TreeWalker walker = traversal.createTreeWalker(doc.getDocumentElement(),
					NodeFilter.SHOW_ELEMENT, null, true);
			this.domTree = createTree(walker);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Retrieve the name of the StateVertix.
	 * 
	 * @return the name of the stateVertix
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the DOM String.
	 * 
	 * @return the dom for this state
	 */
	public String getDom() {
		return dom;
	}

	/**
	 * Retrieve the dom tree
	 * 
	 * @return the dom tree for this state
	 */
	public LblTree getDomTree() {
		return domTree;
	}

	/**
	 * @return the stripped dom by the oracle comparators
	 */
	public String getStrippedDom() {
		return strippedDom;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode of this StateVertix
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if (strippedDom == null || "".equals(strippedDom)) {
			builder.append(dom);
		} else {
			builder.append(strippedDom);
		}

		return builder.toHashCode();
	}

	/**
	 * Compare this vertix to a other StateVertix.
	 * 
	 * @param obj
	 *            the Object to compare this vertix
	 * @return Return true if equal. Uses reflection.
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StateVertix)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final StateVertix rhs = (StateVertix) obj;

		return new EqualsBuilder().append(this.strippedDom, rhs.getStrippedDom()).append(
				this.guidedCrawling, rhs.guidedCrawling).isEquals();
	}

	/**
	 * Returns the name of this state as string.
	 * 
	 * @return a string representation of the current StateVertix
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Return the size of the DOM in bytes.
	 * 
	 * @return the size of the dom
	 */
	public int getDomSize() {
		return getDom().getBytes().length;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param dom
	 *            the dom to set
	 */
	public void setDom(String dom) {
		this.dom = dom;
	}

	/**
	 * @return if this state is created through guided crawling.
	 */
	public boolean isGuidedCrawling() {
		return guidedCrawling;
	}

	/**
	 * @param guidedCrawling
	 *            true if set through guided crawling.
	 */
	public void setGuidedCrawling(boolean guidedCrawling) {
		this.guidedCrawling = guidedCrawling;
	}

	/**
	 * search for new Candidates from this state. The search for candidates is only done when no
	 * list is available yet (candidateActions == null).
	 * 
	 * @param candidateExtractor
	 *            the CandidateElementExtractor to use.
	 * @param crawlTagElements
	 *            the tag elements to examine.
	 * @param crawlExcludeTagElements
	 *            the elements to exclude.
	 * @param clickOnce
	 *            if true examine each element once.
	 * @return true if the searchForCandidateElemens has run false otherwise
	 */
	@GuardedBy("candidateActionsSearchLock")
	public boolean searchForCandidateElements(CandidateElementExtractor candidateExtractor,
                                              List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
                                              boolean clickOnce, StateFlowGraph sfg, boolean isEfficientCrawling, boolean isRandomEventExec, boolean notCrawl) {
		synchronized (candidateActionsSearchLock) {
			if (candidateActions == null) {
				candidateActions = new LinkedBlockingDeque<CandidateCrawlAction>();
			} else {
				return false;
			}
		}
		// TODO read the eventtypes from the crawl elements instead
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add(EventType.click.toString());

		try {
			List<CandidateElement> candidateList =
				candidateExtractor.extract(crawlTagElements, crawlExcludeTagElements,
						clickOnce, this, notCrawl);

			//Amin: 
			numCandidateElements = candidateList.size();

			for (CandidateElement candidateElement : candidateList) {
				for (String eventType : eventTypes) {
					if (eventType.equals(EventType.click.toString())) {
						candidateActions.add(new CandidateCrawlAction(candidateElement,
								EventType.click));
					} else {
						if (eventType.equals(EventType.hover.toString())) {
							candidateActions.add(new CandidateCrawlAction(candidateElement,
									EventType.hover));
						} else {
							LOGGER.warn("The Event Type: " + eventType + " is not supported.");
						}
					}
				}
			}
		} catch (CrawljaxException e) {
			LOGGER.error(
					"Catched exception while searching for candidates in state " + getName(), e);
		}
		return candidateActions.size() > 0; // Only notify of found candidates when there are...

	}

	public List<CandidateElement> searchForTargetElements(CandidateElementExtractor candidateExtractor,
											  List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
											  boolean clickOnce) {
		List<CandidateElement> candidateList = null;
		try {
			candidateList =
					candidateExtractor.extract(crawlTagElements, crawlExcludeTagElements,
							clickOnce, this, true);
		} catch (CrawljaxException e) {
			e.printStackTrace();
		}
		return candidateList;
	}

	/**
	 * Return a list of UnprocessedCandidates in a List.
	 *  
	 * @return a list of candidates which are unprocessed.
	 */
	public List<CandidateElement> getUnprocessedCandidateElements() {
		List<CandidateElement> list = new ArrayList<CandidateElement>();
		if (candidateActions == null) {
			return list;
		}
		CandidateElement last = null;
		for (CandidateCrawlAction candidateAction : candidateActions) {
			if (last != candidateAction.getCandidateElement()) {
				last = candidateAction.getCandidateElement();
				list.add(last);
			}
		}
		return list;
	}
	/**
	 * Removes Candidate Actions on candidateElements that have been 
	 * removed by the pre-state crawl plugin.
	 * 
	 * @param candidateElements 
	 */
	public void filterCandidateActions(List <CandidateElement> candidateElements) {
		if (candidateActions == null) {
			return;
		}
		Iterator iter = candidateActions.iterator();
		CandidateCrawlAction currentAction;
		while (iter.hasNext()) {
			currentAction = (CandidateCrawlAction) iter.next();
			if ( !candidateElements.contains(
					currentAction.getCandidateElement() ) ) {
				iter.remove();  
				//Amin
				numCandidateElements--;
				LOGGER.info("filtered candidate action: " + currentAction.getEventType().name() + " on " + currentAction.getCandidateElement().getGeneralString() );

			}
		}            
	}
	
	
	/**
	 * Prints out dom elements hierarchy - Added by Amin
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void printDOMElements() throws SAXException, IOException{
		Document doc = Helper.getDocument(this.dom);
		DocumentTraversal traversal = (DocumentTraversal) doc;
		TreeWalker walker = traversal.createTreeWalker(doc.getDocumentElement(),
				NodeFilter.SHOW_ELEMENT, null, true);
		traverseLevel(walker, "");
	}

	private static final void traverseLevel(TreeWalker walker, String indent) {
		Node parent = walker.getCurrentNode();
		System.out.println(indent + ((Element) parent).getTagName());
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			traverseLevel(walker, indent + '\t');
		}
		walker.setCurrentNode(parent);
	}

	/**
	 * Prints out dom tree
	 */
	public void printDOmTree(){
		System.out.println(domTree);
	}

	/**
	 * Recursively construct a LblTree from DOM tree - added by Amin 
	 *
	 * @param walker
	 * 			tree walker for DOM tree traversal
	 * @return tree represented by DOM tree
	 */
	public static LblTree createTree(TreeWalker walker) {
		Node parent = walker.getCurrentNode();
		LblTree node = new LblTree(((Element) parent).getTagName(), -1);  // treeID = -1
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			node.add(createTree(walker));
		}
		walker.setCurrentNode(parent);
		return node;
	}

	/**
	 * This is the main work divider function, calling this function will first look at the
	 * registeedCandidateActions to see if the current Crawler has already registered itself at one
	 * of the jobs. Second it tries to see if the current crawler is not already processing one of
	 * the actions and return that action and last it tries to find an unregistered candidate. If
	 * all else fails it tries to return a action that is registered by an other crawler and
	 * disables that crawler.
	 * 
	 * @param requestingCrawler
	 *            the Crawler placing the request for the Action
	 * @param manager
	 *            the manager that can be used to remove a crawler from the queue.
	 * @return the action that needs to be performed by the Crawler.
	 */
	public CandidateCrawlAction pollCandidateCrawlAction(Crawler requestingCrawler,
			CrawlQueueManager manager) {
		// 1.检查registeredCandidateActions中，该requestingCrawler是否还有注册的任务，如果有则返回
		// 2.检查该requestingCrawler是否还有正在进行的任务，如果有则返回
		// 3.检查该StateVertix是否还有未注册的CandidateCrawlAction，如果有则返回
		// 4.上述情况都不能找到一个未执行的CandidateCrawlAction，则从其他注册的crawler中找，并将被找到的crawler从工作队列中移除
		CandidateCrawlAction action = registerdCandidateActions.remove(requestingCrawler);
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		}
		action = workInProgressCandidateActions.get(requestingCrawler);
		if (action != null) {
			return action;
		}
		action = candidateActions.pollFirst();
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		} else {
			Crawler c = registeredCrawlers.pollFirst();
			if (c == null) {
				return null;
			}
			do {
//				if (manager.removeWorkFromQueue(c)) {
//					LOGGER.info("Crawler " + c + " REMOVED from Queue!");
					action = registerdCandidateActions.remove(c);
					if (action != null) {
						/*
						 * We got a action and removed the registeredCandidateActions for the
						 * crawler, remove the crawler from queue as the first thinng. As the
						 * crawler might just have started the run method of the crawler must also
						 * be added with a check hook.
						 */
						LOGGER.info("Stolen work from other Crawler");
						return action;
					} else {
						LOGGER.warn("Oh my! I just removed " + c
								+ " from the queue with no action!");
					}
//				} else {
//					LOGGER.warn("FAILED TO REMOVE " + c + " from Queue!");
//				}
				c = registeredCrawlers.pollFirst();
			} while (c != null);
		}
		return null;
	}

	/**
	 * Register an assignment to the crawler.
	 * 
	 * @param newCrawler
	 *            the crawler that wants an assignment
	 * @return true if the crawler has an assignment false otherwise.
	 */
	public boolean registerCrawler(Crawler newCrawler) {
		if (candidateActions == null) return false;
		CandidateCrawlAction action = candidateActions.pollLast();
		if (action == null) {
			return false;
		}
		registeredCrawlers.offerFirst(newCrawler);
		registerdCandidateActions.put(newCrawler, action);
		return true;
	}

	/**
	 * Register a Crawler that is going to work, tell if his must go on or abort.
	 * 
	 * @param crawler
	 *            the crawler to register
	 * @return true if the crawler is successfully registered
	 */
	public boolean startWorking(Crawler crawler) {
		CandidateCrawlAction action = registerdCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
		if (action == null) {
			return false;
		} else {
			workInProgressCandidateActions.put(crawler, action);
			return true;
		}
	}

	/**
	 * Notify the current StateVertix that the given crawler has finished working on the given
	 * action.
	 * 
	 * @param crawler
	 *            the crawler that is finished
	 * @param action
	 *            the action that have been examined
	 */
	public void finishedWorking(Crawler crawler, CandidateCrawlAction action) {
		candidateActions.remove(action);
		registerdCandidateActions.remove(crawler);
		workInProgressCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
	}
	
	//Amin:  
	public void decreaseCandidateElements(){
		numCandidateElements--;
		//System.out.println("numCandidateElements for state " + this.getName() + " is " + numCandidateElements);
	}
	//Amin: checks is the state is fully expanded. should always be used after 
	public boolean isFullyExpanded(){
		if (numCandidateElements==0)
			return true;
		return false;
	}
	//Amin:  
	public int getNumCandidateElements(){
		return numCandidateElements;
	}

	//Ryan

	public List<String> getKeyContent() {
		return keyContent;
	}

	public void setKeyContent(List<String> keyContent) {
		this.keyContent = keyContent;
		setSimilarScores();
//		LOGGER.info(this.name + " SimilarScoreMap " + similarScoreMap + " " + this.keyContent);
		LOGGER.info(this.name + " SimilarScoreMap " + similarScoreMap);
	}

	public static void setKeyContent(StateVertix originalState, StateVertix currentState) {
		Set<String> originalContentSet = new HashSet<String>();
		if (originalState != null) {
			originalContentSet = Helper.getContentSet(originalState.getDom());
		}
		Set<String> currentContentSet = new HashSet<String>();
		if (currentState != null) {
			currentContentSet = Helper.getContentSet(currentState.getDom());
		}
		Set<String> res = new HashSet<String>();
		for (String s: currentContentSet) {
			if (!originalContentSet.contains(s)) {
				List<String> segmentList = CrawljaxController.segment.getWords(s);
				for (String segment: segmentList) {
					// 过滤掉数字
					if (!Helper.filterSegment(segment)) {
						res.add(segment);
					}
				}
			}
		}
		currentState.setKeyContent(new ArrayList<String>(res));
 	}

	/**
	 * 这种计算方式为，对于所有的状态关键文本内容，计算它们和关键词的最大相似度，并求和取平均
	 * todo
	 * TO-DO: 应该需要改进
	 */
	private void setSimilarScores() {
		List<Keyword> keywords = CrawljaxController.keywords;
		for (int i=0; i<keywords.size(); i++) {
			double sumScore = 0;
			for (String s1: keywords.get(i).getExtendedSeq()) {
				double maxScore = 0;
				for (String s2: keyContent) {
					// TODO: 2020/1/3
					maxScore = Math.max(maxScore, CrawljaxController.word2Vec.getSimWith2Words(s1, s2));
				}
				sumScore += maxScore;
			}
			similarScoreMap.put(i, sumScore / keywords.get(i).getExtendedSeq().size());
		}
	}

	public void setCandidateActions(LinkedBlockingDeque<CandidateCrawlAction> candidateActions) {
		this.candidateActions = candidateActions;
	}

	public LinkedBlockingDeque<Crawler> getRegisteredCrawlers() {
		return registeredCrawlers;
	}

	public void setRegisteredCrawlers(LinkedBlockingDeque<Crawler> registeredCrawlers) {
		this.registeredCrawlers = registeredCrawlers;
	}

	public ConcurrentHashMap<Crawler, CandidateCrawlAction> getRegisterdCandidateActions() {
		return registerdCandidateActions;
	}

	public double getSimilarScore(int pos) {
		if (pos < 0 || pos >= similarScoreMap.size()) {
			throw new RuntimeException("similarScoreMap下标越界");
		}
		return similarScoreMap.get(pos);
	}
}
