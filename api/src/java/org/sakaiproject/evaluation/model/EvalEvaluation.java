
package org.sakaiproject.evaluation.model;

// Generated Mar 20, 2007 10:08:13 AM by Hibernate Tools 3.2.0.beta6a

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;

/**
 * EvalEvaluation generated by hbm2java
 */
public class EvalEvaluation implements java.io.Serializable {

   // Fields    

   private Long id;
   
   private String eid;

   private Date lastModified;

   /**
    * Defines the type of evaluation: use constants EVAL_TYPE_*
    */
   private String type;

   private String owner;

   private String title;

   private String instructions;

   private Date startDate;

   /**
    * This is the ending date for the evalaution
    * NOTE - how do we handle the case of no ending date?
    */
   private Date dueDate;

   /**
    * this defines the grade period for completing the eval after the due date
    */
   private Date stopDate;

   /**
    * The overall view date for this evaluation,
    * Note that this trumps the students and instructors dates and they must be set to be after this one
    * or they will have no effect,
    * TODO - allow this to be set to be the start date (but not earlier) if we want to allow viewing results as we go? 
    */
   private Date viewDate;

   /**
    * if this is null then students cannot view the results of this evaluation,
    * otherwise students can view after this date
    */
   private Date studentsDate;

   /**
    * if this is null instructors cannot view the results of this evaluation,
    * otherwise results can only be viewed after this date
    */
   private Date instructorsDate;

   private String state;

   private String instructorOpt;

   /**
    * the number of days between reminder emails,
    * 0 means emails are disabled
    */
   private Integer reminderDays;

   private String reminderFromEmail;

   private String termId;

   /**
    * if this is set then use the template here,
    * if null then use the default {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}
    */
   private EvalEmailTemplate availableEmailTemplate;

   /**
    * if this is set then use the template here,
    * if null then use the default {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}
    */
   private EvalEmailTemplate reminderEmailTemplate;

   private EvalTemplate template;

   private EvalTemplate addedTemplate;

   private Set<EvalResponse> responses = new HashSet<EvalResponse>(0);

   private Boolean resultsPrivate;

   private Boolean blankResponsesAllowed;

   private Boolean modifyResponsesAllowed;

   private Boolean unregisteredAllowed;

   private Boolean locked;

   private String authControl;

   private String evalCategory;

   // Constructors

   /** default constructor */
   public EvalEvaluation() {
   }

   /** minimal constructor */
   public EvalEvaluation(Date lastModified, String owner, String title, Date startDate, Date dueDate,
         Date stopDate, Date viewDate, String state, Integer reminderDays, EvalTemplate template) {
      this.lastModified = lastModified;
      this.owner = owner;
      this.title = title;
      this.startDate = startDate;
      this.stopDate = stopDate;
      this.dueDate = dueDate;
      this.viewDate = viewDate;
      this.state = state;
      this.reminderDays = reminderDays;
      this.template = template;
   }

   /** full constructor */
   public EvalEvaluation(Date lastModified, String owner, String title, String instructions, Date startDate,
         Date dueDate, Date stopDate, Date viewDate, Date studentsDate, Date instructorsDate, String state,
         String instructorOpt, Integer reminderDays, String reminderFromEmail, String termId,
         EvalEmailTemplate availableEmailTemplate, EvalEmailTemplate reminderEmailTemplate,
         EvalTemplate template, EvalTemplate addedTemplate, Set<EvalResponse> responses, Boolean resultsPrivate,
         Boolean blankResponsesAllowed, Boolean modifyResponsesAllowed, Boolean unregisteredAllowed,
         Boolean locked, String authControl, String evalCategory) {
      this.lastModified = lastModified;
      this.owner = owner;
      this.title = title;
      this.instructions = instructions;
      this.startDate = startDate;
      this.stopDate = stopDate;
      this.dueDate = dueDate;
      this.viewDate = viewDate;
      this.studentsDate = studentsDate;
      this.instructorsDate = instructorsDate;
      this.state = state;
      this.instructorOpt = instructorOpt;
      this.reminderDays = reminderDays;
      this.reminderFromEmail = reminderFromEmail;
      this.termId = termId;
      this.availableEmailTemplate = availableEmailTemplate;
      this.reminderEmailTemplate = reminderEmailTemplate;
      this.template = template;
      this.addedTemplate = addedTemplate;
      this.responses = responses;
      this.resultsPrivate = resultsPrivate;
      this.blankResponsesAllowed = blankResponsesAllowed;
      this.modifyResponsesAllowed = modifyResponsesAllowed;
      this.unregisteredAllowed = unregisteredAllowed;
      this.locked = locked;
      this.authControl = authControl;
      this.evalCategory = evalCategory;
   }

   public EvalTemplate getAddedTemplate() {
      return addedTemplate;
   }

   public void setAddedTemplate(EvalTemplate addedTemplate) {
      this.addedTemplate = addedTemplate;
   }

   public String getAuthControl() {
      return authControl;
   }

   public void setAuthControl(String authControl) {
      this.authControl = authControl;
   }

   public EvalEmailTemplate getAvailableEmailTemplate() {
      return availableEmailTemplate;
   }

   public void setAvailableEmailTemplate(EvalEmailTemplate availableEmailTemplate) {
      this.availableEmailTemplate = availableEmailTemplate;
   }

   public Boolean getBlankResponsesAllowed() {
      return blankResponsesAllowed;
   }

   public void setBlankResponsesAllowed(Boolean blankResponsesAllowed) {
      this.blankResponsesAllowed = blankResponsesAllowed;
   }

   public Date getDueDate() {
      return dueDate;
   }

   public void setDueDate(Date dueDate) {
      this.dueDate = dueDate;
   }

   public String getEvalCategory() {
      return evalCategory;
   }

   public void setEvalCategory(String evalCategory) {
      this.evalCategory = evalCategory;
   }
   
	public String getEid() {
		return this.eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getInstructions() {
      return instructions;
   }

   public void setInstructions(String instructions) {
      this.instructions = instructions;
   }

   public String getInstructorOpt() {
      return instructorOpt;
   }

   public void setInstructorOpt(String instructorOpt) {
      this.instructorOpt = instructorOpt;
   }

   public Date getInstructorsDate() {
      return instructorsDate;
   }

   public void setInstructorsDate(Date instructorsDate) {
      this.instructorsDate = instructorsDate;
   }

   public Date getLastModified() {
      return lastModified;
   }

   public void setLastModified(Date lastModified) {
      this.lastModified = lastModified;
   }

   public Boolean getLocked() {
      return locked;
   }

   public void setLocked(Boolean locked) {
      this.locked = locked;
   }

   public Boolean getModifyResponsesAllowed() {
      return modifyResponsesAllowed;
   }

   public void setModifyResponsesAllowed(Boolean modifyResponsesAllowed) {
      this.modifyResponsesAllowed = modifyResponsesAllowed;
   }

   public String getOwner() {
      return owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public Integer getReminderDays() {
      return reminderDays;
   }

   public void setReminderDays(Integer reminderDays) {
      this.reminderDays = reminderDays;
   }

   public EvalEmailTemplate getReminderEmailTemplate() {
      return reminderEmailTemplate;
   }

   public void setReminderEmailTemplate(EvalEmailTemplate reminderEmailTemplate) {
      this.reminderEmailTemplate = reminderEmailTemplate;
   }

   public String getReminderFromEmail() {
      return reminderFromEmail;
   }

   public void setReminderFromEmail(String reminderFromEmail) {
      this.reminderFromEmail = reminderFromEmail;
   }

   public Set<EvalResponse> getResponses() {
      return responses;
   }

   public void setResponses(Set<EvalResponse> responses) {
      this.responses = responses;
   }

   public Boolean getResultsPrivate() {
      return resultsPrivate;
   }

   public void setResultsPrivate(Boolean resultsPrivate) {
      this.resultsPrivate = resultsPrivate;
   }

   public Date getStartDate() {
      return startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }

   public Date getStopDate() {
      return stopDate;
   }

   public void setStopDate(Date stopDate) {
      this.stopDate = stopDate;
   }

   public Date getStudentsDate() {
      return studentsDate;
   }

   public void setStudentsDate(Date studentsDate) {
      this.studentsDate = studentsDate;
   }

   public EvalTemplate getTemplate() {
      return template;
   }

   public void setTemplate(EvalTemplate template) {
      this.template = template;
   }

   public String getTermId() {
      return termId;
   }

   public void setTermId(String termId) {
      this.termId = termId;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public Boolean getUnregisteredAllowed() {
      return unregisteredAllowed;
   }

   public void setUnregisteredAllowed(Boolean unregisteredAllowed) {
      this.unregisteredAllowed = unregisteredAllowed;
   }

   public Date getViewDate() {
      return viewDate;
   }

   public void setViewDate(Date viewDate) {
      this.viewDate = viewDate;
   }

   
   public String getType() {
      return type;
   }

   
   public void setType(String type) {
      this.type = type;
   }

}
