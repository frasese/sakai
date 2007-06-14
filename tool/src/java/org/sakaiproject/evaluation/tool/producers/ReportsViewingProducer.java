/******************************************************************************
 * ViewReportProducer.java - created by on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.tool.viewparams.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * rendering the report results from an evaluation
 * 
 * @author:Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */

public class ReportsViewingProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    private static Log log = LogFactory.getLog(EvaluationBean.class);

    public static final String VIEW_ID = "report_view";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalEvaluationsLogic evalsLogic;
    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
    }

    private EvalResponsesLogic responsesLogic;
    public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
        this.responsesLogic = responsesLogic;
    }

    private EvalItemsLogic itemsLogic;
    public void setItemsLogic(EvalItemsLogic itemsLogic) {
        this.itemsLogic = itemsLogic;
    }

    public ReportsBean reportsBean;
    public void setReportsBean(ReportsBean reportsBean) {
        this.reportsBean = reportsBean;
    }

    int displayNumber = 1;

    String[] groupIds;

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        String currentUserId = externalLogic.getCurrentUserId();

        UIMessage.make(tofill, "view-report-title", "viewreport.page.title");

        ReportParameters reportViewParams = (ReportParameters) viewparams;
        Long evaluationId = reportViewParams.evaluationId;
        if (evaluationId != null) {

            // bread crumbs
            UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));
            UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 
                    new TemplateViewParameters(ReportChooseGroupsProducer.VIEW_ID, reportViewParams.evaluationId));

            EvalEvaluation evaluation = evalsLogic.getEvaluationById(reportViewParams.evaluationId);

            // do a permission check
            if (!currentUserId.equals(evaluation.getOwner()) && 
                    !externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
                throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
            }

            // get template from DAO 
            EvalTemplate template = evaluation.getTemplate();

            List allTemplateItems = new ArrayList(template.getTemplateItems());
            if (!allTemplateItems.isEmpty()) {
                if (reportViewParams.groupIds == null || reportViewParams.groupIds.length == 0) {
                    // TODO - this is a security hole -AZ
                    // no passed in groups so just list all of them
                    Map evalGroups = evalsLogic.getEvaluationGroups(new Long[] { evaluationId }, false);
                    List groups = (List) evalGroups.get(evaluationId);
                    groupIds = new String[groups.size()];
                    for (int i = 0; i < groups.size(); i++) {
                        EvalGroup currGroup = (EvalGroup) groups.get(i);
                        groupIds[i] = currGroup.evalGroupId;
                    }
                } else {
                    // use passed in group ids
                    groupIds = reportViewParams.groupIds;
                }

                UIInternalLink.make(tofill, "fullEssayResponse", UIMessage.make("viewreport.view.essays"), new EssayResponseParams(
                        ReportsViewEssaysProducer.VIEW_ID, reportViewParams.evaluationId, groupIds));
                UIInternalLink.make(tofill, "csvResultsReport", UIMessage.make("viewreport.view.csv"), new CSVReportViewParams(
                        "csvResultsReport", template.getId(), reportViewParams.evaluationId, groupIds)); //$NON-NLS-3$

                // filter out items that cannot be answered (header, etc.)
                List answerableItemsList = TemplateItemUtils.getAnswerableTemplateItems(allTemplateItems);

                UIBranchContainer courseSection = null;
                UIBranchContainer instructorSection = null;

                // handle showing all course type items
                if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, answerableItemsList)) {
                    courseSection = UIBranchContainer.make(tofill, "courseSection:");
                    UIMessage.make(courseSection, "report-course-questions", "viewreport.itemlist.coursequestions");
                    for (int i = 0; i < answerableItemsList.size(); i++) {
                        EvalTemplateItem tempItem1 = (EvalTemplateItem) answerableItemsList.get(i);

                        String cat = tempItem1.getItemCategory();
                        UIBranchContainer radiobranch = null;

                        if (cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {
                            radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", i + "");
                            if (i % 2 == 1)
                                radiobranch.decorators = new DecoratorList(new UIColourDecorator(null, Color
                                        .decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

                            renderTemplateItemResults(tempItem1, evaluation.getId(), displayNumber, radiobranch, courseSection);
                            displayNumber++;
                        }
                    }
                }

                // handle showing all instructor type items
                if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, answerableItemsList)) {
                    instructorSection = UIBranchContainer.make(tofill, "instructorSection:");
                    UIMessage.make(instructorSection, "report-instructor-questions", "viewreport.itemlist.instructorquestions");
                    for (int i = 0; i < answerableItemsList.size(); i++) {
                        EvalTemplateItem tempItem1 = (EvalTemplateItem) answerableItemsList.get(i);
                        String cat = tempItem1.getItemCategory();
                        UIBranchContainer radiobranch = null;

                        if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) {
                            radiobranch = UIBranchContainer.make(instructorSection, "itemrow:first", i + "");
                            if (i % 2 == 1)
                                radiobranch.decorators = new DecoratorList(new UIColourDecorator(null, Color
                                        .decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

                            renderTemplateItemResults(tempItem1, evaluation.getId(), i, radiobranch, instructorSection);
                            displayNumber++;
                        }
                    } // end of for loop				
                    //}
                }
            }
        } else {
            // invalid view params
            throw new IllegalArgumentException("Evaluation id is required to view report");
        }

    }

    private void renderTemplateItemResults(EvalTemplateItem myTempItem, Long evalId, int i, UIBranchContainer radiobranch, UIContainer tofill) {

        EvalItem myItem = myTempItem.getItem();

        if (TemplateItemUtils.getTemplateItemType(myTempItem).equals(EvalConstants.ITEM_TYPE_SCALED)) {
            //normal scaled type
            EvalScale scale = myItem.getScale();
            String[] scaleOptions = scale.getOptions();
            int optionCount = scaleOptions.length;
            //	String scaleValues[] = new String[optionCount];
            String scaleLabels[] = new String[optionCount];

            Boolean useNA = myTempItem.getUsesNA();

            UIBranchContainer scaledSurvey = UIBranchContainer.make(radiobranch, "scaledSurvey:");

            UIOutput.make(scaledSurvey, "itemNum", (new Integer(i)).toString());
            UIOutput.make(scaledSurvey, "itemText", myItem.getItemText());

            if (useNA.booleanValue() == true) {
                UIBranchContainer radiobranch3 = UIBranchContainer.make(scaledSurvey, "showNA:");
                UIBoundBoolean.make(radiobranch3, "itemNA", useNA);
            }

            List itemAnswers = responsesLogic.getEvalAnswers(myItem.getId(), evalId, groupIds);

            for (int x = 0; x < scaleLabels.length; x++) {
                UIBranchContainer answerbranch = UIBranchContainer.make(scaledSurvey, "answers:", x + "");
                UIOutput.make(answerbranch, "responseText", scaleOptions[x]);
                int answers = 0;
                //count the number of answers that match this one
                for (int y = 0; y < itemAnswers.size(); y++) {
                    EvalAnswer curr = (EvalAnswer) itemAnswers.get(y);
                    if (curr.getNumeric().intValue() == x) {
                        answers++;
                    }
                }
                UIOutput.make(answerbranch, "responseTotal", answers + "", x + "");
            }

        } else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) { //"Short Answer/Essay"
            UIBranchContainer essay = UIBranchContainer.make(radiobranch, "essayType:");
            UIOutput.make(essay, "itemNum", i + "");
            UIOutput.make(essay, "itemText", myItem.getItemText());

            UIInternalLink.make(essay, "essayResponse", 
                    new EssayResponseParams(ReportsViewEssaysProducer.VIEW_ID, evalId, myTempItem.getId(), groupIds));
        } else {
            log.warn("Skipped invalid item type: TI: " + myTempItem.getId() + ", Item: " + myItem.getId() + ", type: " + myItem.getClassification());
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ReportParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    public List reportNavigationCases() {
        List i = new ArrayList();
        return i;
    }

}
