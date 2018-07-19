package listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;

public class ContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

    /**
     * Finds a translation of the given phrase. <br/>
     * The search is case-sensitive and no pre-processing is applied (trim, normalize, etc.)
     * 
     * @param phrase The exact phrase to translate
     * @param language Language key as stored in the translations file
     * @return The translated phrase if found, the phrase itself otherwise.
     */
    public static String translate(final String phrase, final String language) {
        return phrase;
    }

	/**
	 * Finds a translation of the given phrase. <br/>
	 * The search is case-sensitive and no pre-processing is applied (trim, normalize, etc.)
	 *
	 * @param phrase
	 *            The exact phrase to translate
	 * @param session
	 *            to obtain the language code from
	 * @return The translated phrase if found, the phrase itself otherwise.
	 * @see {@link SessionListener#LANGUAGE_ATTRIBUTE_NAME}
	 */
	public static String translate(final String phrase, final HttpSession session) {
		return phrase;
	}

}
