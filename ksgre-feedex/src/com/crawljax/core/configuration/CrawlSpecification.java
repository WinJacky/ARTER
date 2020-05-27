package com.crawljax.core.configuration;

import com.crawljax.condition.Condition;
import com.crawljax.condition.browserwaiter.ExpectedCondition;
import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlStrategy;
import com.crawljax.core.Crawler;
import com.crawljax.core.TagElement;
import com.crawljax.core.oraclecomparator.Comparator;
import com.crawljax.core.oraclecomparator.OracleComparator;
import com.crawljax.core.state.Eventable;
import javafx.event.EventType;

import java.util.*;

/**
 * 指定爬取过程中的一些可选属性。用户必须指定哪些 html 元素可以被点击。
 * <p/>
 * 爬取的范围可以用{@link #setDepth(int)}以及{@link #setMaximumStates(int)}来限制.<br/>
 * 爬取的时间可以用{@link #setMaximumRuntime(int)}来限制.
 * <p/>
 * 在缺省情况下，Crwaljax 使用随机值来填充表单元素。
 * {@link #setRandomInputInForms(boolean)}.<br/>
 * 为表单元素指定输入可以使用{@link #setInputSpecification(InputSpecification)} 来定义.
 */
public class CrawlSpecification {

    private static final int DEFAULT_MAXIMUMRUNTIME = 3600;
    private static final int DEFAULT_WAITTIMEAFTERRELOADURL = 500;
    private static final int DEFAULT_WAITTIMEAFTEREVENT = 500;

    private final String url;

    private List<Eventable.EventType> crawlEvents = new ArrayList<Eventable.EventType>();

    private int depth = 2;
    private int maximumStates = 0;
    private int maximumRuntime = DEFAULT_MAXIMUMRUNTIME; // in seconds
    private int waitTimeAfterReloadUrl = DEFAULT_WAITTIMEAFTERRELOADURL; // in milliseconds
    private int waitTimeAfterEvent = DEFAULT_WAITTIMEAFTEREVENT; // in milliseconds
    private final CrawlActions crawlActions = new CrawlActions();

    // set it true if you wish to do diverse crawling - added by Amin
    private boolean diverseCrawling = false;
    public boolean isDiverseCrawling() {
        return diverseCrawling;
    }
    public void setDiverseCrawling(boolean diverseCrawling) {
        this.diverseCrawling = diverseCrawling;
    }

    // set it true if you wish to do efficient crawling - added by Amin
    // Note if efficientCrawling is set to true diverseCrawling would be also set to true automatically since it uses diversity metric
    private boolean efficientCrawling = false;

    // set it true if you wish to randomly execute events - added by Amin
    private boolean randomEventExec = false;
    public boolean isEfficientCrawling() {
        return efficientCrawling;
    }
    public void setEfficientCrawling(boolean efficientCrawling) {
        this.efficientCrawling = efficientCrawling;
        if (efficientCrawling == true)
            setDiverseCrawling(true);
    }
    public void setRandomEventExec(boolean randomEventExec) {
        this.randomEventExec = randomEventExec;
    }
    public boolean isRandomEventExec() {
        return randomEventExec;
    }



    private boolean randomInputInForms = true;
    private InputSpecification inputSpecification = new InputSpecification();

    private boolean testInvariantsWhileCrawling = true;
    private final List<Invariant> invariants = new ArrayList<Invariant>();

    private final List<OracleComparator> oracleComparators = new ArrayList<OracleComparator>();
    private final List<WaitCondition> waitConditions = new ArrayList<WaitCondition>();
    private final List<CrawlCondition> crawlConditions = new ArrayList<CrawlCondition>();

    //private boolean clicklOnce = false;
    private boolean clicklOnce = true; // default

    private final List<String> ignoredFrameIdentifiers = new ArrayList<String>();
    private boolean disableCrawlFrames = false;

    private boolean domMutationNotifierPluginCheck = false;

    // 默认爬取方式DFS
    private CrawlStrategy crawlStrategy = CrawlStrategy.DFS;

    private Map<TagElement, List<TagElement>> preClickConstraintMap = new HashMap<TagElement, List<TagElement>>();

    /**
     * @param url
     *            the site to crawl
     */
    public CrawlSpecification(String url) {
        this.crawlEvents.add(Eventable.EventType.click);
        this.url = url;
    }

    /**
     * Specifies that Crawljax should click all the default clickable elements. These include: All
     * anchor tags All buttons
     */
    public void clickDefaultElements() {
        crawlActions.click("a");
        crawlActions.click("button");
        crawlActions.click("input").withAttribute("type", "submit");
        crawlActions.click("input").withAttribute("type", "button");
    }

    /**
     * Set of HTML elements Crawljax will click during crawling For exmple 1) <a.../> 2) <div/>
     * click("a") will only include 1 This set can be restricted by {@link #dontClick(String)}.
     *
     * @param tagName
     *            the tag name of the elements to be included
     * @return this CrawlElement
     */
    public CrawlElement click(String tagName) {
        return crawlActions.click(tagName);
    }

    /**
     * Set of HTML elements Crawljax will NOT examine during crawling When an HTML is present in the
     * click and dontClick sets, then the element will not be clicked. For example: 1) <a
     * href="#">Some text</a> 2) <a class="foo" .../> 3) <div class="foo" .../> click("a")
     * dontClick("a").withAttribute("class", "foo"); Will include only include HTML element 2
     *
     * @param tagName
     *            the tag name of the elements to be excluded
     * @return this CrawlElement
     */
    public CrawlElement dontClick(String tagName) {
        return crawlActions.dontClick(tagName);
    }

    /**
     * Crawljax will the HTML elements while crawling if and only if all the specified conditions
     * are satisfied. IMPORTANT: only works with click()!!! For example:
     * when(onContactPageCondition) will only click the HTML element if it is on the contact page
     *
     * @param conditions
     *            the condition to be met.
     * @return this CrawlActions
     */
    public CrawlActions when(Condition... conditions) {
        return crawlActions.when(conditions);
    }

    /**
     * @return the initial url of the site to crawl
     */
    protected String getUrl() {
        return url;
    }

    /**
     * @return the maximum crawl depth
     */
    protected int getDepth() {
        return depth;
    }

    /**
     * Sets the maximum crawl depth. 1 is one click, 2 is two clicks deep, ...
     *
     * @param crawlDepth
     *            the maximum crawl depth. 0 to ignore
     */
    public void setDepth(int crawlDepth) {
        this.depth = crawlDepth;
    }

    /**
     * @return the crawlMaximumStates
     */
    protected int getMaximumStates() {
        return maximumStates;
    }

    /**
     * Sets the maximum number of states. Crawljax will stop crawling when this maximum number of
     * states are found
     *
     * @param crawlMaximumStates
     *            the maximum number of states. 0 specifies no bound for the number of crawl states.
     */
    public void setMaximumStates(int crawlMaximumStates) {
        this.maximumStates = crawlMaximumStates;
    }

    /**
     * @return the crawlMaximumRuntime
     */
    protected int getMaximumRuntime() {
        return maximumRuntime;
    }

    /**
     * Sets the maximum time for Crawljax to run. Crawljax will stop crawling when this timelimit is
     * reached.
     *
     * @param seconds
     *            the crawlMaximumRuntime to set
     */
    public void setMaximumRuntime(int seconds) {
        this.maximumRuntime = seconds;
    }

    /**
     * @return whether to Crawljax should enter random values in form input fields
     */
    protected boolean getRandomInputInForms() {
        return randomInputInForms;
    }

    /**
     * @param value
     *            whether to Crawljax should enter random values in form input fields
     */
    public void setRandomInputInForms(boolean value) {
        this.randomInputInForms = value;
    }

    /**
     * @return the number of milliseconds to wait after reloading the url
     */
    protected int getWaitTimeAfterReloadUrl() {
        return waitTimeAfterReloadUrl;
    }

    /**
     * @param milliseconds
     *            the number of milliseconds to wait after reloading the url
     */
    public void setWaitTimeAfterReloadUrl(int milliseconds) {
        this.waitTimeAfterReloadUrl = milliseconds;
    }

    /**
     * @return the number the number of milliseconds to wait after an event is fired
     */
    protected int getWaitTimeAfterEvent() {
        return waitTimeAfterEvent;
    }

    /**
     * @param milliseconds
     *            the number of milliseconds to wait after an event is fired
     */
    public void setWaitTimeAfterEvent(int milliseconds) {
        this.waitTimeAfterEvent = milliseconds;
    }

    /**
     * @return the events that should be fired (e.g. onclick)
     */
    protected List<Eventable.EventType> getCrawlEvents() {
        return crawlEvents;
    }

    /**
     * @return the inputSpecification in which data for input field is specified
     */
    protected InputSpecification getInputSpecification() {
        return inputSpecification;
    }

    /**
     * @param inputSpecification
     *            in which data for input fields is specified
     */
    public void setInputSpecification(InputSpecification inputSpecification) {
        this.inputSpecification = inputSpecification;
    }

    /**
     * @return the different crawl actions.
     */
    protected CrawlActions crawlActions() {
        return crawlActions;
    }

    /**
     * @return the oracleComparators
     */
    protected List<OracleComparator> getOracleComparators() {
        return oracleComparators;
    }

    /**
     * Adds the Oracle Comparator to the list of comparators.
     *
     * @param id
     *            a name for the Oracle Comparator.
     * @param oracleComparator
     *            the oracle to be added.
     */
    public void addOracleComparator(String id, Comparator oracleComparator) {
        this.oracleComparators.add(new OracleComparator(id, oracleComparator));
    }

    /**
     * Adds an Oracle Comparator with preconditions to the list of comparators.
     *
     * @param id
     *            a name for the Oracle Comparator
     * @param oracleComparator
     *            the oracle to be added.
     * @param preConditions
     *            the preconditions to be met.
     */
    public void addOracleComparator(String id, Comparator oracleComparator,
                                    Condition... preConditions) {
        this.oracleComparators.add(new OracleComparator(id, oracleComparator, preConditions));
    }

    /**
     * @return the invariants
     */
    protected List<Invariant> getInvariants() {
        return invariants;
    }

    /**
     * @param description
     *            the description of the invariant.
     * @param condition
     *            the condition to be met.
     */
    public void addInvariant(String description, Condition condition) {
        this.invariants.add(new Invariant(description, condition));
    }

    /**
     * @param description
     *            the description of the invariant.
     * @param condition
     *            the invariant condition.
     * @param preConditions
     *            the precondition.
     */
    public void addInvariant(String description, Condition condition, Condition... preConditions) {
        this.invariants.add(new Invariant(description, condition, preConditions));
    }

    /**
     * @return whether invariants should be tested while crawling.
     */
    protected boolean getTestInvariantsWhileCrawling() {
        return testInvariantsWhileCrawling;
    }

    /**
     * @param testInvariantsWhileCrawling
     *            whether invariants should be tested while crawling
     */
    public void setTestInvariantsWhileCrawling(boolean testInvariantsWhileCrawling) {
        this.testInvariantsWhileCrawling = testInvariantsWhileCrawling;
    }

    /**
     * @return the waitConditions
     */
    protected List<WaitCondition> getWaitConditions() {
        return waitConditions;
    }

    /**
     * @param url
     *            the full url or a part of the url where should be waited for the
     *            expectedConditions
     * @param expectedConditions
     *            the conditions to wait for.
     */
    public void waitFor(String url, ExpectedCondition... expectedConditions) {
        this.waitConditions.add(new WaitCondition(url, expectedConditions));
    }

    /**
     * @param url
     *            the full url or a part of the url where should be waited for the
     *            expectedConditions
     * @param expectedConditions
     *            the conditions to wait for
     * @param timeout
     *            the timeout
     */
    public void waitFor(String url, int timeout, ExpectedCondition... expectedConditions) {
        this.waitConditions.add(new WaitCondition(url, timeout, expectedConditions));
    }

    /**
     * @return the crawlConditions
     */
    protected List<CrawlCondition> getCrawlConditions() {
        return crawlConditions;
    }

    /**
     * @param description
     *            the description
     * @param crawlCondition
     *            the condition
     */
    public void addCrawlCondition(String description, Condition crawlCondition) {
        this.crawlConditions.add(new CrawlCondition(description, crawlCondition));
    }

    /**
     * @param description
     *            the description
     * @param crawlCondition
     *            the condition
     * @param preConditions
     *            the preConditions
     */
    public void addCrawlCondition(String description, Condition crawlCondition,
                                  Condition... preConditions) {
        this.crawlConditions.add(new CrawlCondition(description, crawlCondition, preConditions));
    }

    /**
     * @return the crawl once value.
     */
    protected boolean getClickOnce() {
        return this.clicklOnce;
    }

    /**
     * @param clickOnce
     *            the crawl once value;
     */
    public void setClickOnce(boolean clickOnce) {
        this.clicklOnce = clickOnce;
    }

    /**
     * @param crawlEvents
     *            the crawlEvents to set
     */
    public void setCrawlEvents(List<Eventable.EventType> crawlEvents) {
        this.crawlEvents = crawlEvents;
    }

    /**
     * @param string
     *            the frame identifier to ignore when descending into (i)frames
     */
    public void dontCrawlFrame(String string) {
        this.ignoredFrameIdentifiers.add(string);
    }

    /**
     * @return the list of ignored frames
     */
    protected List<String> ignoredFrameIdentifiers() {
        return ignoredFrameIdentifiers;
    }

    /**
     * disable the crawling of Frames in total.
     */
    public void disableCrawlFrames() {
        this.disableCrawlFrames = true;
    }

    /**
     * Is the crawling of Frames enabled.
     *
     * @return true if frames should be crawled false otherwise.
     */
    protected boolean isCrawlFrames() {
        return !disableCrawlFrames;
    }

    /**
     * Is there any plugin for by-passing Dom Comparison
     *
     * @return true if there is one, false otherwise.
     */

    public boolean getDomMutationNotifierPluginCheck() {
        return domMutationNotifierPluginCheck;

    }

    public void setDomMutationNotifierPluginCheck(boolean value) {
        this.domMutationNotifierPluginCheck = value;
    }

    public CrawlStrategy getCrawlStrategy() {
        return crawlStrategy;
    }

    public void setCrawlStrategy(CrawlStrategy crawlStrategy) {
        this.crawlStrategy = crawlStrategy;
    }

    public Map<TagElement, List<TagElement>> getPreClickConstraintMap() {
        return preClickConstraintMap;
    }

    public CrawlSpecification addPreClickConstraint(TagElement tagElement, TagElement... tagElements) {
        preClickConstraintMap.put(tagElement, Arrays.asList(tagElements));
        return this;
    }
}
