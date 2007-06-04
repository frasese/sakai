/******************************************************************************
 * EvalEmailsLogicImpl.java - created by aaronz@vt.edu on Dec 29, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.utils.TextTemplateLogicUtils;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * EvalEmailsLogic implementation
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImpl implements EvalEmailsLogic {

	private static Log log = LogFactory.getLog(EvalEmailsLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private EvalAssignsLogic assignsLogic;
	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
		this.assignsLogic = assignsLogic;
	}

	private EvalEvaluationsLogic evaluationLogic;
	public void setEvaluationLogic(EvalEvaluationsLogic evaluationLogic) {
		this.evaluationLogic = evaluationLogic;
	}
	
	private EvalResponsesLogic evalResponsesLogic;
	public void setEvalResponsesLogic(EvalResponsesLogic evalResponsesLogic) {
		this.evalResponsesLogic = evalResponsesLogic;
	}

	// INIT method
	public void init() {
		log.debug("Init");
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#saveEmailTemplate(org.sakaiproject.evaluation.model.EvalEmailTemplate, java.lang.String)
	 */
	public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId) {
		log.debug("userId: " + userId + ", emailTemplate: " + emailTemplate.getId());

		// set the date modified
		emailTemplate.setLastModified( new Date() );

		// check user permissions
		if (! canUserControlEmailTemplate(userId, emailTemplate)) {
			throw new SecurityException("User ("+userId+
					") cannot control email template ("+emailTemplate.getId()+
					") without permissions");
		}

		// checks to keeps someone from overwriting the default templates
		if (emailTemplate.getId() == null) {
			// null out the defaultType for new templates 
			emailTemplate.setDefaultType(null);

		} else {
			// existing template
			if (emailTemplate.getDefaultType() != null) {
				throw new IllegalArgumentException("Cannot modify default templates or set existing templates to be default");
			}

			// check if there are evaluation this is used in and if the user can modify this based on them
			// check available templates
			List l = dao.findByProperties(EvalEvaluation.class, 
					new String[] {"availableEmailTemplate.id"}, 
					new Object[] {emailTemplate.getId()} );
			for (int i = 0; i < l.size(); i++) {
				EvalEvaluation eval = (EvalEvaluation) l.get(i);
				// check eval/template permissions
				checkEvalTemplateControl(userId, eval, emailTemplate);
			}
			// check reminder templates
			l = dao.findByProperties(EvalEvaluation.class, 
					new String[] {"reminderEmailTemplate.id"}, 
					new Object[] {emailTemplate.getId()} );
			for (int i = 0; i < l.size(); i++) {
				EvalEvaluation eval = (EvalEvaluation) l.get(i);
				// check eval/template permissions
				checkEvalTemplateControl(userId, eval, emailTemplate);
			}
		}

		// save the template if allowed
		dao.save(emailTemplate);
		log.info("User ("+userId+") saved email template ("+emailTemplate.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#getDefaultEmailTemplate(int)
	 */
	public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateTypeConstant) {
		log.debug("emailTemplateTypeConstant: " + emailTemplateTypeConstant);
		
		// check and get type
		String templateType;
		if (EvalConstants.EMAIL_TEMPLATE_CREATED.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_CREATED;
		} else if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE;
		} else if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE_OPT_IN;	
		} else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER;
		} else if (EvalConstants.EMAIL_TEMPLATE_RESULTS.equals(emailTemplateTypeConstant)) {
				templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_RESULTS;	
		} else {
			throw new IllegalArgumentException("Invalid emailTemplateTypeConstant: " + emailTemplateTypeConstant);			
		}

		// fetch template by type
		List l = dao.findByProperties(EvalEmailTemplate.class, 
				new String[] {"defaultType"}, 
				new Object[] {templateType} );
		if (l.isEmpty()) {
			throw new IllegalStateException("Could not find any default template for type constant: " +
					emailTemplateTypeConstant);
		}
		return (EvalEmailTemplate) l.get(0);
	}


	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#canControlEmailTemplate(java.lang.String, java.lang.Long, int)
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, String emailTemplateTypeConstant) {
		log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateTypeConstant: " + emailTemplateTypeConstant);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// check the type constant
		EvalEmailTemplate emailTemplate;
		if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
			emailTemplate = eval.getAvailableEmailTemplate();
		} else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
			emailTemplate = eval.getReminderEmailTemplate();
		} else {
			throw new IllegalArgumentException("Invalid emailTemplateTypeConstant: " + emailTemplateTypeConstant);			
		}

		// check the permissions and state
		try {
			return checkEvalTemplateControl(userId, eval, emailTemplate);
		} catch (RuntimeException e) {
			log.info( e.getMessage() );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#canControlEmailTemplate(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId) {
		log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateId: " + emailTemplateId);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the email template
		EvalEmailTemplate emailTemplate = (EvalEmailTemplate) dao.findById(EvalEmailTemplate.class, emailTemplateId);
		if (emailTemplate == null) {
			throw new IllegalArgumentException("Cannot find email template with this id: " + emailTemplateId);
		}

		// make sure this template is associated with this evaluation
		if ( emailTemplate.equals( eval.getAvailableEmailTemplate() ) ) {
			log.debug("template matches available template from eval ("+eval.getId()+")");
		} else if ( emailTemplate.equals( eval.getReminderEmailTemplate() ) ) {
			log.debug("template matches reminder template from eval ("+eval.getId()+")");
		} else {
			throw new IllegalArgumentException("email template ("+
					emailTemplate.getId()+") does not match any template from eval ("+eval.getId()+")");
		}

		// check the permissions and state
		try {
			return checkEvalTemplateControl(userId, eval, emailTemplate);
		} catch (RuntimeException e) {
			log.info( e.getMessage() );
		}
		return false;
	}

	// INTERNAL METHODS

	/**
	 * Check if user can control an email template
	 * @param userId
	 * @param emailTemplate
	 * @return true if they can
	 */
	protected boolean canUserControlEmailTemplate(String userId, EvalEmailTemplate emailTemplate) {
		if ( externalLogic.isUserAdmin(userId) ) {
			return true;
		} else if ( emailTemplate.getOwner().equals(userId) ) {
			return true;
		}
		return false;
	}

	/**
	 * Check if user can control evaluation and template combo
	 * @param userId
	 * @param eval
	 * @param emailTemplate
	 * @return true if they can, throw exceptions otherwise
	 */
	protected boolean checkEvalTemplateControl(String userId, EvalEvaluation eval, EvalEmailTemplate emailTemplate) {
		log.debug("userId: " + userId + ", evaluationId: " + eval.getId());

		if ( EvalUtils.getEvaluationState(eval) == EvalConstants.EVALUATION_STATE_INQUEUE ) {
			if (emailTemplate == null) {
				// currently using the default templates so check eval perms

				// check eval user permissions (just owner and super at this point)
				// TODO - find a way to centralize this check
				if (userId.equals(eval.getOwner()) ||
						externalLogic.isUserAdmin(userId)) {
					return true;
				} else {
					throw new SecurityException("User ("+userId+") cannot control email template in evaluation ("+eval.getId()+"), do not have permission");
				}
			} else {
				// check email template perms
				if (canUserControlEmailTemplate(userId, emailTemplate)) {
					return true;
				} else {
					throw new SecurityException("User ("+userId+") cannot control email template ("+emailTemplate.getId()+") without permissions");
				}
			}
		} else {
			throw new IllegalStateException("Cannot modify email template in running evaluation ("+eval.getId()+")");
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalCreatedNotifications(java.lang.Long, boolean)
	 */
	public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner) {
		log.debug("evaluationId: " + evaluationId + ", includeOwner: " + includeOwner);
		
		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}
		
		// get the email template header
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_CREATED);
		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_CREATED);
		}
		
		// append opt-in, opt-out, and/or add questions messages followed by footer
		String message = modifyCreatedEmailMessage(eval, emailTemplate);

		// get the associated contexts for this evaluation
		Map evalGroups = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, true);

		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroups.get(evaluationId);
		log.debug("Found " + groups.size() + " contexts for new evaluation: " + evaluationId);

		List sentMessages = new ArrayList();
		// loop through contexts and send emails to correct users in each evalGroupId
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			Set userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED);
			if (! includeOwner && userIdsSet.contains(eval.getOwner())) {
				userIdsSet.remove(eval.getOwner());
			}
			
			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_CREATED + 
					" notification to for new evaluation (" + evaluationId + 
					") and evalGroupId (" + group.evalGroupId + ")");
			
			//set template to modified template
			emailTemplate.setMessage(message);
			
			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);
					
			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this evalGroupId
			externalLogic.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation created message to " + toUserIds.length + " users");
		}

		return (String[]) sentMessages.toArray( new String[] {} );
	}

	/**
	 * Add a message about opting in or out, or adding items
	 * between message header and footer as appropriate
	 * @param eval the EvalEvaluation
	 * @param emailTemplate the default EvalEmailTemplat
	 * @return the modified EvalEmailTempate message
	 */
	private String modifyCreatedEmailMessage(EvalEvaluation eval, EvalEmailTemplate emailTemplate) {
		
		int opt = (EvalConstants.EMAIL_CREATED_OPT_IN_TEXT.length() > EvalConstants.EMAIL_CREATED_OPT_OUT_TEXT.length()) ?
				EvalConstants.EMAIL_CREATED_OPT_IN_TEXT.length() : EvalConstants.EMAIL_CREATED_OPT_OUT_TEXT.length();
		int max = emailTemplate.getMessage().length() + opt +
			EvalConstants.EMAIL_CREATED_ADD_ITEMS_TEXT.length() + EvalConstants.EMAIL_CREATED_DEFAULT_TEXT_FOOTER.length();
		StringBuffer sb = new StringBuffer();
		sb.ensureCapacity(max);
		int addItems = ((Integer)settings.get(EvalSettings.ADMIN_ADD_ITEMS_NUMBER)).intValue();
		sb.append(emailTemplate.getMessage());
		if(!eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED) || (addItems > 0))
		{
			if(eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN)) {
				//if eval is opt-in notify instructors that they may opt in
				sb.append(EvalConstants.EMAIL_CREATED_OPT_IN_TEXT);
			}
			else if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_OUT)) {
				//if eval is opt-out notify instructors that they may opt out
				sb.append(EvalConstants.EMAIL_CREATED_OPT_OUT_TEXT);
			}
			if(addItems > 0) {
				//if eval allows instructors to add questions notify instructors they may add questions
				sb.append(EvalConstants.EMAIL_CREATED_ADD_ITEMS_TEXT);
			}
		}
		sb.append(EvalConstants.EMAIL_CREATED_DEFAULT_TEXT_FOOTER);
		return new String(sb.toString());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableNotifications(java.lang.Long, boolean)
	 */
	public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees);
		
		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );
		Set userIdsSet = null;
		String message = null;
		boolean studentNotification = true;

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the student email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
		}
		
		// get the instructor opt-in email template
		EvalEmailTemplate emailOptInTemplate = null;
		if(eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN)) {
			emailOptInTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN );
			if (emailOptInTemplate == null) {
				throw new IllegalStateException("Cannot find email opt-in template: " + EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN);
			}
		}

		//get the associated assign groups for this evaluation
		Map evalAssignGroups = evaluationLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
		List assignGroups = (List) evalAssignGroups.get(evaluationId);

		List sentMessages = new ArrayList();
		// loop through groups and send emails to correct users group
		for (int i=0; i<assignGroups.size(); i++) {
			EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(i);
			EvalGroup group = externalLogic.makeEvalGroupObject( assignGroup.getEvalGroupId() );
			if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
				//notify students
				userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
				studentNotification = true;
			}
			else {
				//instructor may opt-in or opt-out
				if (assignGroup.getInstructorApproval().booleanValue()) {
					//instructor has opted-in, notify students
					userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
					studentNotification = true;
					break;
				} else {
					if (eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_OPT_IN) && includeEvaluatees) {
						// instructor has not opted-in, notify instructors
						userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED);
						studentNotification = false;
						break;
					}
				}
			}

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_CREATED + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);

			//choose from 2 templates
			if(studentNotification)
				message = makeEmailMessage(emailTemplate.getMessage(), 
						eval, group, replacementValues);
			else
				message = makeEmailMessage(emailOptInTemplate.getMessage(), 
						eval, group, replacementValues);

			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this evalGroupId
			// TODO - internationalize these messages
			externalLogic.sendEmails(from, toUserIds, 
					"New evaluation created: " + eval.getTitle(), message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");
		} //groups

		return (String[]) sentMessages.toArray( new String[] {} );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)
	 */
	public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId) {
		
		//TODO with above refactor extract method
		
		if (evaluationId == null) {
			throw new IllegalStateException("EvalEvaluation id parameter is null");
		}
		if (evalGroupId == null) {
			throw new IllegalStateException("EvalGroup id is null");
		}
		
		//get group
		Long[] evaluationIds = new Long[] {evaluationId};
		Map evalGroups = evaluationLogic.getEvaluationGroups(evaluationIds, false);
		EvalGroup group = (EvalGroup)evalGroups.get(evaluationId);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}
		
		// get email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
		}
		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );
		List sentMessages = new ArrayList();
		
		//get student ids
		Set userIdsSet = externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
		if (userIdsSet.size() == 0) return (String[])sentMessages.toArray(new String[] {});
		String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
		
		// replace the text of the template with real values
		Map replacementValues = new HashMap();
		replacementValues.put("HelpdeskEmail", from);
		String message = makeEmailMessage(emailTemplate.getMessage(), 
				eval, group, replacementValues);
		// store sent messages to return
		sentMessages.add(message);

		// send the actual emails for this evalGroupId
		externalLogic.sendEmails(from, 
				toUserIds, 
				"New evaluation created: " + eval.getTitle(), 
				message);
		log.info("Sent evaluation available message to " + toUserIds.length + " users");
		return (String[]) sentMessages.toArray( new String[] {} );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalReminderNotifications(java.lang.Long, java.lang.String)
	 */
	public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
		log.debug("evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);
		if(includeConstant == null || 
				!(includeConstant == EvalConstants.EMAIL_INCLUDE_NONTAKERS || includeConstant ==  EvalConstants.EMAIL_INCLUDE_ALL)) {
			log.error("includeConstant null or unknown");
			return null;
		}

		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );
		
		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}
		
		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_REMINDER );

		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_REMINDER);
		}

		List sentMessages = new ArrayList();

		// get the associated eval groups for this evaluation
		// NOTE: this only returns the groups that should get emails, there is no need to do an additional check
		// to see if the instructor has opted in in this case -AZ
		Map evalGroupIds = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, false);

		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroupIds.get(evaluationId);
		log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
		
		Set userIdsSet = new HashSet();

		// loop through groups and send emails to correct users in each
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			
			userIdsSet.clear();
			if(EvalConstants.EMAIL_INCLUDE_NONTAKERS.equals(includeConstant)) {
				userIdsSet.addAll(evalResponsesLogic.getNonResponders(evaluationId, group));
			}
			else if(EvalConstants.EMAIL_INCLUDE_ALL.equals(includeConstant)) {
				userIdsSet.addAll(evalResponsesLogic.getNonResponders(evaluationId, group));
				userIdsSet.addAll(externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED));
			}

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;
			
			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_REMINDER + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);
			
			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this evalGroupId
			externalLogic.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");
		}//groups
		return (String[]) sentMessages.toArray( new String[] {} );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalResultsNotifications(java.lang.Long, boolean, boolean)
	 */
	public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees, boolean includeAdmins) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees + ", includeAdmins: " + includeAdmins);
		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );
		
		/*TODO deprecated?
		if(EvalConstants.EMAIL_INCLUDE_ALL.equals(includeConstant)) {
		}
		boolean includeEvaluatees = true;
		if (includeEvaluatees) {
			// TODO Not done yet
			log.error("includeEvaluatees Not implemented");
		}
		*/

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}
		
		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_RESULTS);

		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_RESULTS);
		}

		List sentMessages = new ArrayList();

		// get the associated eval groups for this evaluation
		Map evalGroupIds = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, false);

		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroupIds.get(evaluationId);
		log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
		Set userIdsSet = new HashSet();
		
		// loop through contexts and send emails to correct users in each evalGroupId
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			userIdsSet.clear();
			if(includeEvaluatees) {
				userIdsSet.addAll(externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION));
			}
			if(includeAdmins) {
				userIdsSet.addAll(externalLogic.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED));
			}
			
			//if results are private just send to owner
			if(eval.getResultsPrivate().booleanValue()) {
				userIdsSet.add(eval.getOwner());
			}
			
			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_REMINDER + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);

			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this evalGroupId
			externalLogic.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");

		}
		return (String[]) sentMessages.toArray( new String[] {} );
	}



	/**
	 * Builds the email message from a template and a bunch of variables
	 * (passed in and otherwise)
	 * 
	 * @param messageTemplate
	 * @param eval
	 * @param group
	 * @param replacementValues a map of String -> String representing $keys in the template to replace with text values
	 * @return the processed message template with replacements and logic handled
	 */
	private String makeEmailMessage(String messageTemplate, EvalEvaluation eval, EvalGroup group, Map replacementValues) {
		// replace the text of the template with real values
		if (replacementValues == null) {
			replacementValues = new HashMap();
		}
		replacementValues.put("EvalTitle", eval.getTitle() );

		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance( DateFormat.MEDIUM, externalLogic.getUserLocale(externalLogic.getCurrentUserId()) );

		replacementValues.put("EvalStartDate", df.format(eval.getStartDate()) );
		replacementValues.put("EvalDueDate", df.format(eval.getDueDate()) );
		replacementValues.put("EvalResultsDate", df.format(eval.getViewDate()) );
		replacementValues.put("EvalGroupTitle", group.title);

		// generate URLs to the evaluation
		String evalEntityURL = null;
		if (group != null && group.evalGroupId != null) {
			// get the URL directly to the evaluation with group context included
			EvalAssignGroup assignGroup = assignsLogic.getAssignGroupById( assignsLogic.getAssignGroupId(eval.getId(), group.evalGroupId) );
			if (assignGroup != null) {
				evalEntityURL = externalLogic.getEntityURL(assignGroup);
			}
		}

		if (evalEntityURL == null) {
			// just get the URL to the evaluation without group context
			evalEntityURL = externalLogic.getEntityURL(eval);
		}

		// TODO check these URLS once I can get the right tool url - rwellis
		
		// all URLs are identical because the user permissions determine access uniquely
		replacementValues.put("URLtoTakeEval", evalEntityURL);
		replacementValues.put("URLtoAddItems", evalEntityURL);
		replacementValues.put("URLtoOptIn", evalEntityURL);
		replacementValues.put("URLtoOptOut", evalEntityURL);
		replacementValues.put("URLtoViewResults", evalEntityURL);
		replacementValues.put("URLtoSystem", externalLogic.getServerUrl());

		return TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
	}

}
