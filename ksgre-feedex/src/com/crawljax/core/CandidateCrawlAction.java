package com.crawljax.core;

import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.Helper;
import main.java.nlp.utils.SplitWords;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

/**
 * 该类组合了CandidateElement和Eventype.
 * This class corresponds the combination of a CandidateElement and a single eventType.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: CandidateCrawlAction.java 197 2010-01-30 02:45:24Z amesbah $
 */
public class CandidateCrawlAction {
	private final CandidateElement candidateElement;
	private final EventType eventType;

	private List<CandidateCrawlAction> preClickActionList = new ArrayList<CandidateCrawlAction>();

	// 可点击元素的关键文本内容（分词后的），包括了元素的内含文本、属性值（不包含属性名）
	private List<String> keywordList;

	// <关键词序列下标，语义相似度得分>
	private Map<Integer, Double> similarScoreMap = new HashMap<Integer, Double>();

	private boolean clickForPrePos = false;

	/**
	 * The Constructor for the CandidateCrawlAction, build a new instance with the CandidateElement
	 * and the EventType.
	 * 
	 * @param candidateElement
	 *            the element to execute the eventType on
	 * @param eventType
	 *            the eventType to execute on the Candidate Element.
	 */
	public CandidateCrawlAction(CandidateElement candidateElement, EventType eventType) {
		this.candidateElement = candidateElement;
		this.eventType = eventType;

		/* 2020-1-3 */
		// 提取事件文本关键词
		if (CrawljaxController.checkKSG()) {
			Element element = candidateElement.getElement();
			Set<String> set = new HashSet<>();
			if (StringUtils.isNotEmpty(element.getTextContent())) {
				set.add(Helper.removeNewLines(element.getTextContent()).trim());
			}
			for (int i=0; i<element.getAttributes().getLength(); i++) {
				Node attr = element.getAttributes().item(i);
				if (!StringUtils.equals("href", attr.getNodeName().toLowerCase())
					&& !StringUtils.equals("class", attr.getNodeName().toLowerCase())
					&& StringUtils.isNotBlank(attr.getNodeValue())) {
					set.add(Helper.removeNewLines(attr.getNodeValue()).trim());
				}
			}
			Set<String> keywordSet = new HashSet<>();
			for (String s: set) {
				List<String> segmentList = SplitWords.getWords(s);
				for (String segment: segmentList) {
					if (!Helper.filterSegment(segment)) {
						keywordSet.add(segment);
					}
				}
			}
			this.keywordList = new ArrayList<>(keywordSet);
			setSimilarScores();
		}
		/* 2020-1-3 */
	}

	private void setSimilarScores() {
		List<Keyword> keywords = CrawljaxController.keywords;
		for (int i=0; i<keywords.size(); i++) {
			double sumScore = 0;
			for (String s1: keywords.get(i).getExtendedSeq()) {
				double maxScore = 0;
				for (String s2: keywordList) {
					maxScore = Math.max(maxScore, CrawljaxController.word2Vec.getSimWith2Words(s1, s2));
				}
				sumScore += maxScore;
			}
			similarScoreMap.put(i, sumScore / keywords.get(i).getExtendedSeq().size());
		}
	}

	public double getSimilarScore(int pos) {
		if (pos < 0 || pos >= similarScoreMap.size()) {
			throw new RuntimeException("similarScoreMap下标越界");
		}
		clickForPrePos = false;
		return similarScoreMap.get(pos);
	}

	/**
	 * @return the candidateElement
	 */
	public final CandidateElement getCandidateElement() {
		return candidateElement;
	}

	/**
	 * @return the eventType
	 */
	public final EventType getEventType() {
		return eventType;
	}

	public List<CandidateCrawlAction> getPreClickActionList() {
		return preClickActionList;
	}

	public void setPreClickActionList(List<CandidateCrawlAction> preClickActionList) {
		this.preClickActionList = preClickActionList;
	}

	/**
	 * 获取当前关键词下标以及上一下标的相似度的最大值，原因在于某一关键词可能不仅仅只对应一个状态，即使该关键词已经被发现了，下个状态可能仍然对应该关键词
	 * @param pos
	 * @return
	 */
	public double getIntervalSimilarScore(int pos) {
		if (pos < 0 || pos >= similarScoreMap.size()) {
			return 0;
		}
		double pre = Threshold.INTERVAL_W.getValue() * similarScoreMap.getOrDefault(pos-1, new Double(0));
		double cur = similarScoreMap.get(pos);
		if (pre > cur) {
			clickForPrePos = true;
		} else {
			clickForPrePos = false;
		}
		return Math.max(pre, cur);
	}

	public boolean isClickForPrePos() {
		return clickForPrePos;
	}
}
