package util;

import static action.AServlet.MESSAGE_COLLECTION_ATTRIBUTE_NAME;

import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;

import domain.UserMessage;
import listener.ContextListener;

public final class UserMessageUtils {

	private UserMessageUtils() {

	}
	
	@SuppressWarnings("unchecked")
	private static final Collection<UserMessage> getUserMessageCollection(final HttpServletRequest request) {
		final Object messageCollectionObj = request.getAttribute(MESSAGE_COLLECTION_ATTRIBUTE_NAME);
		if (messageCollectionObj != null && Collection.class.isAssignableFrom(messageCollectionObj.getClass())) {
			return (Collection<UserMessage>) messageCollectionObj;
		} else {
			final Collection<UserMessage> errorCollection = new LinkedList<>();
			request.setAttribute(MESSAGE_COLLECTION_ATTRIBUTE_NAME, errorCollection);
			return errorCollection;
		}
	}

	public static void addUserErrorMessage(final Logger logger, final HttpServletRequest request, final String message, final IStringTransformer stringTransformer) {
		logger.error(stringTransformer.transform(message));
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.ERROR, stringTransformer);
	}

	public static void addUserErrorMessage(final Logger logger, final HttpServletRequest request, final String message, final Exception e, final IStringTransformer stringTransformer) {
		logger.error(stringTransformer.transform(message), e);
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.ERROR, stringTransformer);
	}

	public static void addUserWarningMessage(final Logger logger, final HttpServletRequest request, final String message, final IStringTransformer stringTransformer) {
		logger.warn(stringTransformer.transform(message));
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.WARNING, stringTransformer);
	}

	public static void addUserWarningMessage(final Logger logger, final HttpServletRequest request, final String message, final Exception e, final IStringTransformer stringTransformer) {
		logger.warn(stringTransformer.transform(message), e);
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.WARNING, stringTransformer);
	}

	public static void addUserInfoMessage(final Logger logger, final HttpServletRequest request, final String message, final IStringTransformer stringTransformer) {
		logger.info(stringTransformer.transform(message));
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.INFO, stringTransformer);
	}

	public static void addUserSuccessMessage(final Logger logger, final HttpServletRequest request, final String message, final IStringTransformer stringTransformer) {
		logger.info(stringTransformer.transform(message));
		UserMessageUtils.addUserMessage(request, message, UserMessage.Status.SUCCESS, stringTransformer);
	}

	public static void addUserMessage(final HttpServletRequest request, final String message, final UserMessage.Status severity, final IStringTransformer stringTransformer) {
		UserMessageUtils.addLocalizedUserMessage(request, ContextListener.translate(message, request.getSession(false)), severity, stringTransformer);
	}

	public static void addUserMessage(final HttpServletRequest request, final String message, final UserMessage.Status severity) {
		UserMessageUtils.addLocalizedUserMessage(request, ContextListener.translate(message, request.getSession(false)), severity, IStringTransformer.ECHO);
	}

	private static void addLocalizedUserMessage(final HttpServletRequest request, final String messageTranslated, final UserMessage.Status severity, final IStringTransformer stringTransformer) {
		UserMessageUtils.getUserMessageCollection(request).add(
			new UserMessage(severity, stringTransformer.transform(messageTranslated)));
	}
}
