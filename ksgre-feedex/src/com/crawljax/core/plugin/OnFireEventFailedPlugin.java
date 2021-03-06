package com.crawljax.core.plugin;

import com.crawljax.core.state.Eventable;

import java.util.List;

/**
 * Plugin type that is called every time event that was requested to fire failed firing.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: OnFireEventFailedPlugin.java 228 2010-02-05 19:17:02Z dannyroest $
 */
public interface OnFireEventFailedPlugin extends Plugin {

	/**
	 * Method that is called when an event that was requested to fire failed firing.
	 * 
	 * @param eventable
	 *            the eventable that failed to execute
	 * @param pathToFailure
	 *            the list of eventable lead TO this failed eventable, the eventable excluded.
	 */
	void onFireEventFailed(Eventable eventable, List<Eventable> pathToFailure);

}
