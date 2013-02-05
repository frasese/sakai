package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.util.FormattedText;
import org.xcri.Extension;
import org.xcri.common.Description;
import org.xcri.common.ExtensionManager;
import org.xcri.common.OverrideManager;
import org.xcri.common.descriptive.Regulations;
import org.xcri.core.Catalog;
import org.xcri.core.Course;
import org.xcri.core.Presentation;
import org.xcri.core.Provider;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.presentation.Venue;

import uk.ac.ox.oucs.vle.xcri.daisy.Bookable;
import uk.ac.ox.oucs.vle.xcri.daisy.CourseSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentThirdLevelApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.DepartmentalSubUnit;
import uk.ac.ox.oucs.vle.xcri.daisy.DivisionWideEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeEmail;
import uk.ac.ox.oucs.vle.xcri.daisy.EmployeeName;
import uk.ac.ox.oucs.vle.xcri.daisy.Identifier;
import uk.ac.ox.oucs.vle.xcri.daisy.ModuleApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.OtherDepartment;
import uk.ac.ox.oucs.vle.xcri.daisy.Sessions;
import uk.ac.ox.oucs.vle.xcri.daisy.SupervisorApproval;
import uk.ac.ox.oucs.vle.xcri.daisy.TeachingDetails;
import uk.ac.ox.oucs.vle.xcri.daisy.TermCode;
import uk.ac.ox.oucs.vle.xcri.daisy.TermLabel;
import uk.ac.ox.oucs.vle.xcri.daisy.WebAuthCode;
import uk.ac.ox.oucs.vle.xcri.oxcap.MemberApplyTo;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapCourse;
import uk.ac.ox.oucs.vle.xcri.oxcap.OxcapPresentation;
import uk.ac.ox.oucs.vle.xcri.oxcap.Session;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;

public class XcriOxCapPopulatorImpl implements Populator {

	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
	 * The class to get the inputStream.
	 */
	private PopulatorInput populatorInput;
	public void setPopulatorInput(PopulatorInput populatorInput) {
		this.populatorInput = populatorInput;
	}

	private static final Log log = LogFactory.getLog(XcriOxCapPopulatorImpl.class);

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy  hh:mm");

	static {
		ExtensionManager.registerExtension(new WebAuthCode());
		ExtensionManager.registerExtension(new DepartmentalSubUnit());
		ExtensionManager.registerExtension(new DepartmentThirdLevelApproval());
		ExtensionManager.registerExtension(new DivisionWideEmail());
		ExtensionManager.registerExtension(new CourseSubUnit());
		ExtensionManager.registerExtension(new ModuleApproval());
		ExtensionManager.registerExtension(new SupervisorApproval());
		ExtensionManager.registerExtension(new OtherDepartment());
		ExtensionManager.registerExtension(new Sessions());
		ExtensionManager.registerExtension(new Bookable());
		ExtensionManager.registerExtension(new TermCode());
		ExtensionManager.registerExtension(new TermLabel());
		ExtensionManager.registerExtension(new EmployeeName());
		ExtensionManager.registerExtension(new EmployeeEmail());
		ExtensionManager.registerExtension(new Identifier());
		ExtensionManager.registerExtension(new MemberApplyTo());
		ExtensionManager.registerExtension(new TeachingDetails());
		ExtensionManager.registerExtension(new Subject());
		ExtensionManager.registerExtension(new Session());

		OverrideManager.registerOverride(Course.class, new OxcapCourse());
		OverrideManager.registerOverride(Presentation.class, new OxcapPresentation());
	}

	/**
	 * @throws MalformedURLException 
	 * 
	 */
	public void update(PopulatorContext context) throws PopulatorException {

		try {
			InputStream input = populatorInput.getInput(context);

			if (null == input) {
				throw new PopulatorException("No Input for Importer");
			}
			process(context.getName(), input);

		} catch (MalformedURLException e) {
			log.warn("MalformedURLException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IllegalStateException e) {
			log.warn("IllegalStateException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IOException e) {
			log.warn("IOException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (JDOMException e) {
			log.warn("JDOMException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (InvalidElementException e) {
			log.warn("InvalidElementException ["+context.getURI()+"]", e);
			throw new PopulatorException(e.getLocalizedMessage());
		}

	}

	/**
	 * 
	 * @param inputStream
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws InvalidElementException 
	 */
	public void process(String name, InputStream inputStream) 
			throws JDOMException, IOException, InvalidElementException {

		Catalog catalog = new Catalog();
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(inputStream);
		catalog.fromXml(document);

		XcriOxcapPopulatorInstanceData data = 
				new XcriOxcapPopulatorInstanceData(proxy, name, simpleDateFormat.format(catalog.getGenerated()));

		Provider[] providers = catalog.getProviders();

		for (Provider provider : providers) {
			provider(provider, data);		
		}

		data.endTasks();

	}

	/**
	 * 
	 * @param provider
	 * @param createGroups
	 * @throws IOException 
	 */
	private void provider(Provider provider, XcriOxcapPopulatorInstanceData data) 
			throws IOException {

		String departmentName = null;
		if (provider.getTitles().length > 0) {
			departmentName = provider.getTitles()[0].getValue();
		}
		String departmentCode = null;
		String divisionEmail = null;
		boolean departmentApproval = false;
		String divisionCode = null;
		Set<String> departmentApprovers = new HashSet<String>();
		Set<String> divisionSuperUsers = new HashSet<String>();
		Map<String, String> subunits = new HashMap<String, String>();

		for (Extension extension : provider.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if (typeProviderId(identifier.getType())) {
					if (typeProviderFallbackId(identifier.getType()) &&
							null != departmentCode) {
						continue;
					}
					departmentCode = identifier.getValue();
					continue;
				}
				if (typeProviderDivision(identifier.getType())) {
					divisionCode = identifier.getValue();
					continue;
				}
			}

			if (extension instanceof DivisionWideEmail) {
				divisionEmail = extension.getValue();
				continue;
			}

			if (extension instanceof DepartmentThirdLevelApproval) {
				departmentApproval = parseBoolean(extension.getValue());
				continue;
			}

			if (extension instanceof ModuleApproval) {
				departmentApprovers.add(getUser(extension.getValue()));
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;

				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.superUser) {
					divisionSuperUsers.add(getUser(webAuthCode.getValue()));
				}
				continue;
			}

			if (extension instanceof DepartmentalSubUnit) {
				DepartmentalSubUnit subUnit = (DepartmentalSubUnit) extension;
				subunits.put(subUnit.getCode(), subUnit.getValue());
				continue;
			}

		}

		if (null == departmentCode) {
			data.logMe(
					"Log Failure Provider ["+departmentCode+":"+departmentName+"] No Provider Identifier");
			return;
		}

		data.incrDepartmentSeen();
		if (updateDepartment(departmentCode, departmentName, departmentApproval, 
				departmentApprovers)) {
			data.incrDepartmentCreated();;
		} else {
			data.incrDepartmentUpdated();
		}

		for (Map.Entry<String, String> entry : subunits.entrySet()) {
			data.incrSubunitSeen();
			if (updateSubUnit(entry.getKey(), entry.getValue(), departmentCode)) {
				data.incrSubunitCreated();;
			} else {
				data.incrSubunitUpdated();
			}
		}

		for (Course course : provider.getCourses()) {
			course(course, departmentCode, departmentName, divisionEmail, divisionSuperUsers, data);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeProviderId(String type) {
		if ("ns:department".equals(type) ||
				"ns:twoThree".equals(type)) {
			return true;
		}
		return false;
	}

	protected static boolean typeProviderFallbackId(String type) {
		if ("ns:department".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeProviderDivision(String type) {
		if ("ns:division".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * Process <course> tag
	 * 
	 * @param course
	 * @param departmentCode
	 * @param departmentName
	 * @param divisionEmail
	 * @param divisionSuperUsers
	 * @param createComponents
	 * @throws IOException 
	 */
	private void course(Course course, 
			String departmentCode, String departmentName, 
			String divisionEmail, 
			Set<String> divisionSuperUsers,
			XcriOxcapPopulatorInstanceData data) 
					throws IOException {

		CourseGroupDAO myCourse = new CourseGroupDAO();

		myCourse.setSource(data.getFeed());
		myCourse.setDept(departmentCode);
		myCourse.setDepartmentName(departmentName);
		myCourse.setContactEmail(divisionEmail);
		myCourse.setTitle(course.getTitles()[0].getValue());

		OxcapCourse oxCourse = (OxcapCourse)course;
		myCourse.setVisibility(oxCourse.getVisibility().toString());

		if (course.getRegulations().length > 0) {
			Regulations xRegulations = course.getRegulations()[0];
			if (!xRegulations.isXhtml()) {
				myCourse.setRegulations(parse(xRegulations.getValue()));
			} else {
				myCourse.setRegulations(xRegulations.getValue());
			}
		}

		Set<Subject> researchCategories = new HashSet<Subject>();
		Set<Subject> skillsCategories = new HashSet<Subject>();
		Set<Subject> jacsCategories = new HashSet<Subject>();

		String teachingcomponentId = null;
		Set<String> administrators = new HashSet<String>();
		Set<String> otherDepartments = new HashSet<String>();

		for (Extension extension : course.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if (typeCourseId(identifier.getType())) {
					myCourse.setCourseId(identifier.getValue());
				}
				if ("teachingComponentId".equals(identifier.getType())) {
					teachingcomponentId = identifier.getValue();
				}
				continue;
			}

			if (extension instanceof SupervisorApproval) {
				myCourse.setSupervisorApproval(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof ModuleApproval) {
				myCourse.setAdministratorApproval(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof CourseSubUnit) {
				CourseSubUnit subUnit = (CourseSubUnit)extension;
				myCourse.setSubunit(subUnit.getCode());
				myCourse.setSubunitName(subUnit.getValue());
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.administrator) {
					administrators.add(getUser(webAuthCode.getValue()));
				}
				continue;
			}

			if (extension instanceof OtherDepartment) {
				if (!extension.getValue().isEmpty()) {
					otherDepartments.add(extension.getValue());
				}
				continue;
			}

			if (extension instanceof Subject) {
				Subject subject = (Subject) extension;

				if (subject.isRDFCategory()) {
					skillsCategories.add(subject);
				}
				if (subject.isRMCategory()) {
					researchCategories.add(subject);
				}
				if (subject.isJACSCategory()) {
					jacsCategories.add(subject);
				}
				continue;
			}
		}

		myCourse.setAdministrators(administrators);
		myCourse.setOtherDepartments(otherDepartments);
		myCourse.setSuperusers(divisionSuperUsers);

		if (null == myCourse.getCourseId()) {
			data.logMe(
					"Log Failure Course ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] No Course Identifier");
			return;
		}

		if (course.getDescriptions().length > 0) {
			Description xDescription = course.getDescriptions()[0];
			if (!xDescription.isXhtml()) {
				myCourse.setDescription(parse(xDescription.getValue()));
			} else {
				myCourse.setDescription(xDescription.getValue());
			}
		} else {
			data.logMe(
					"Log Warning Course ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] has no description");
		}

		if (!myCourse.getCourseId().equals(data.getLastGroup())) {

			data.incrGroupSeen();
			data.setLastGroup(myCourse.getCourseId());

			if (validCourse(data, myCourse, researchCategories, skillsCategories, jacsCategories)) {

				if (updateCourse(data, myCourse, researchCategories, skillsCategories, jacsCategories)) {
					data.incrGroupCreated();
				} else {
					data.incrGroupUpdated();
				}
			}
		}

		Presentation[] presentations = course.getPresentations();
		for (int i=0; i<presentations.length; i++) {
			presentation(presentations[i], myCourse.getCourseId(), teachingcomponentId, data);
		}

	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typeCourseId(String type) {
		if ("ns:daisy-course".equals(type) ||
				"ns:itlp-course".equals(type) ||
				"ns:careers-course".equals(type) ||
				"ns:language-centre-course".equals(type) ||
				"ns:medsci-course".equals(type) ||
				"ns:sharepoint-course".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param presentation
	 * @param teachingcomponentId
	 * @param groups
	 * @throws IOException 
	 */
	private void presentation(Presentation presentation, 
			String assessmentunitCode, String teachingcomponentId, XcriOxcapPopulatorInstanceData data) 
					throws IOException {

		CourseComponentDAO myPresentation = new CourseComponentDAO();

		myPresentation.setComponentId(teachingcomponentId);
		myPresentation.setSource(data.getFeed());
		myPresentation.setTitle(presentation.getTitles()[0].getValue());

		if (null != presentation.getAttendanceMode()) {
			myPresentation.setAttendanceMode(presentation.getAttendanceMode().getIdentifier());
			myPresentation.setAttendanceModeText(presentation.getAttendanceMode().getValue());
		}

		if (null != presentation.getAttendancePattern()) {
			myPresentation.setAttendancePattern(presentation.getAttendancePattern().getIdentifier());
			myPresentation.setAttendancePatternText(presentation.getAttendancePattern().getValue());
		}

		if (null != presentation.getApplyTo()) {
			myPresentation.setApplyTo(presentation.getApplyTo().getValue());
		}

		if (null != presentation.getStart()) {
			myPresentation.setStarts(presentation.getStart().getDtf());
			myPresentation.setStartsText(presentation.getStart().getValue());
		}

		if (null != presentation.getEnd()) {
			myPresentation.setEnds(presentation.getEnd().getDtf());
			myPresentation.setEndsText(presentation.getEnd().getValue());
		}

		if (null != presentation.getApplyFrom()) {
			myPresentation.setOpens(presentation.getApplyFrom().getDtf());
			myPresentation.setOpensText(presentation.getApplyFrom().getValue());
		}

		if (null != presentation.getApplyUntil()) {
			myPresentation.setCloses(presentation.getApplyUntil().getDtf());
			myPresentation.setClosesText(presentation.getApplyUntil().getValue());
		}

		if (0 != presentation.getVenues().length) {
			Venue venue = presentation.getVenues()[0];
			if (null != venue.getProvider() && venue.getProvider().getTitles().length > 0) {
				myPresentation.setLocation(venue.getProvider().getTitles()[0].getValue());
			}
		}

		String teacherId = null;
		Collection<Session> sessions = new HashSet<Session>();

		for (Extension extension : presentation.getExtensions()) {

			if (extension instanceof Identifier) {
				Identifier identifier = (Identifier) extension;
				if ("presentationURI".equals(identifier.getType())) {
					//uri = identifier.getValue();
					continue;
				}
				if (typePresentationId(identifier.getType())) {
					myPresentation.setPresentationId(identifier.getValue());
					continue;
				}
			}

			if (extension instanceof Bookable) {
				myPresentation.setBookable(parseBoolean(extension.getValue()));
				continue;
			}

			if (extension instanceof EmployeeName) {
				myPresentation.setTeacherName(extension.getValue());
				continue;
			}

			if (extension instanceof EmployeeEmail) {
				myPresentation.setTeacherEmail(extension.getValue());
				continue;
			}

			if (extension instanceof MemberApplyTo) {
				myPresentation.setMemberApplyTo(extension.getValue());
				continue;
			}

			if (extension instanceof Sessions) {
				myPresentation.setSessions(extension.getValue());
				continue;
			}

			if (extension instanceof TermCode) {
				myPresentation.setTermcode(extension.getValue());
				continue;
			}

			if (extension instanceof TermLabel) {
				myPresentation.setSlot(extension.getValue());
				continue;
			}

			if (extension instanceof TeachingDetails) {
				myPresentation.setTeachingDetails(extension.getValue());
				continue;
			}

			if (extension instanceof WebAuthCode) {
				WebAuthCode webAuthCode = (WebAuthCode) extension;
				if (webAuthCode.getWebAuthCodeType() == WebAuthCode.WebAuthCodeType.presenter) {
					teacherId = webAuthCode.getValue();
				}
				continue;
			}

			if (extension instanceof Session) {
				Session session = (Session)extension;
				if (session.getIdentifiers().length > 0) {
					sessions.add(session);
					continue;
				}
			}

		}

		if (null != presentation.getPlaces() &&
				!presentation.getPlaces().getValue().isEmpty()) {
			try {
				myPresentation.setSize(Integer.parseInt(presentation.getPlaces().getValue()));
			} catch (Exception e) {
				data.logMe(
						"Log Warning Presentation ["+
								myPresentation.getPresentationId()+":"+myPresentation.getTitle()+
								"] value in places tag is not a number ["+presentation.getPlaces().getValue()+"]");
			}
		}

		CourseGroupDAO courseDao = dao.findCourseGroupById(assessmentunitCode);

		data.incrComponentSeen();

		if (validComponent(data, myPresentation, teacherId, 
				(Set<Session>) sessions, courseDao)) {

			updateComponent(data, myPresentation, teacherId, 
					(Set<Session>) sessions, courseDao);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	protected static boolean typePresentationId(String type) {
		if ("ns:daisy-presentation".equals(type) ||
				"ns:careers-presentation".equals(type) ||
				"ns:itlp-presentation".equals(type) ||
				"ns:language-centre-presentation".equals(type) ||
				"ns:medsci-presentation".equals(type) ||
				"ns:sharepoint-presentation".equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param code
	 * @param name
	 * @param approve
	 * @param approvers
	 * @return
	 */
	private boolean updateDepartment(String code, String name, boolean approve, Set<String> approvers) {

		log.debug("XcriPopulatorImpl.updateDepartment ["+code+":"+name+":"+
				approve+":"+approvers.size()+"]");

		boolean created = false;

		if (null != dao) {
			CourseDepartmentDAO departmentDao = dao.findDepartmentByCode(code);

			if (null == departmentDao) {
				departmentDao = new CourseDepartmentDAO(code);
				created = true;
			}
			departmentDao.setName(name);
			departmentDao.setApprove(approve);
			departmentDao.setApprovers(approvers);
			dao.save(departmentDao);
		}

		return created;
	}

	/**
	 * 
	 * @param code
	 * @param name
	 * @param departmentCode
	 * @return
	 */
	private boolean updateSubUnit(String code, String name, String departmentCode) {

		log.debug("XcriPopulatorImpl.updateSubUnit ["+
				code+":"+name+":"+departmentCode+"]");

		boolean created = false;

		if (null != dao) {
			CourseSubunitDAO subunitDao = dao.findSubunitByCode(code);
			if (null == subunitDao) {
				subunitDao = new CourseSubunitDAO(code);
				created = true;
			}
			subunitDao.setSubunitName(name);
			subunitDao.setDepartmentCode(departmentCode);
			dao.save(subunitDao);
		}

		return created;
	}

	/**
	 * 
	 * @param data
	 * @param myCourse
	 * @param researchCategories
	 * @param skillsCategories
	 * @param jacsCategories
	 * @return
	 */
	protected boolean validCourse(XcriOxcapPopulatorInstanceData data, 
			CourseGroupDAO myCourse,
			Set<Subject> researchCategories, Set<Subject> skillsCategories, Set<Subject> jacsCategories) {

		int i=0;

		try {
			if (null == myCourse.getCourseId()) {
				logMe(data, "Log Failure Assessment Unit ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"] No AssessmentUnit code");
				i++;
			}

			if (i == 0) {
				return true;
			}

		} catch (IOException e) {

		}

		return false;
	}

	/**
	 * 
	 * @param data
	 * @param myCourse
	 * @param researchCategories
	 * @param skillsCategories
	 * @param jacsCategories
	 * @return
	 * @throws IOException
	 */
	private boolean updateCourse(XcriOxcapPopulatorInstanceData data,
			CourseGroupDAO myCourse,
			Set<Subject> researchCategories, 
			Set<Subject> skillsCategories, 
			Set<Subject> jacsCategories) throws IOException {

		boolean created = false;

		if (null != dao) {
			CourseGroupDAO groupDao = dao.findCourseGroupById(myCourse.getCourseId());

			if (groupDao == null) {
				groupDao = dao.newCourseGroup(myCourse.getCourseId(), myCourse.getTitle(), myCourse.getDept(), myCourse.getSubunit());
				created = true;
			} else {
				groupDao.setDept(myCourse.getDept());
				groupDao.setSubunit(myCourse.getSubunit());
				groupDao.setTitle(myCourse.getTitle());
			}
			groupDao.setDescription(myCourse.getDescription());
			groupDao.setDepartmentName(myCourse.getDepartmentName());
			groupDao.setSubunitName(myCourse.getSubunitName());
			groupDao.setVisibility(myCourse.getVisibility());
			groupDao.setSource(myCourse.getSource());
			groupDao.setSupervisorApproval(myCourse.getSupervisorApproval());
			groupDao.setAdministratorApproval(myCourse.getAdministratorApproval());
			groupDao.setContactEmail(myCourse.getContactEmail());
			groupDao.setAdministrators(myCourse.getAdministrators());
			groupDao.setRegulations(myCourse.getRegulations());
			groupDao.setDeleted(false);
			groupDao.setSuperusers(myCourse.getSuperusers());
			groupDao.setOtherDepartments(myCourse.getOtherDepartments());

			Set<CourseCategoryDAO> categories = new HashSet<CourseCategoryDAO>();
			for (Subject subject : researchCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.RM, subject.getIdentifier(), subject.getValue()));
			}
			for (Subject subject : skillsCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.RDF, subject.getIdentifier(), subject.getValue()));
			}
			for (Subject subject : jacsCategories) {
				categories.add(new CourseCategoryDAO(
						CourseGroup.Category_Type.JACS, subject.getIdentifier(), subject.getValue()));
			}

			//remove unwanted categories
			// done this way to avoid java.util.ConcurrentModificationException 
			for (Iterator<CourseCategoryDAO> itr = groupDao.getCategories().iterator(); itr.hasNext();) {
				CourseCategoryDAO category = itr.next();
				if (!categories.contains(category)) {
					itr.remove();
				}
			}

			//add any new categories
			for (CourseCategoryDAO category : categories) {
				if (!groupDao.getCategories().contains(category)) {
					groupDao.getCategories().add(category);
				}
			}

			dao.save(groupDao);
		}

		if (created) {
			logMs(data, "Log Success Course Group created ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"]");
		} else {
			logMs(data, "Log Success Course Group updated ["+myCourse.getCourseId()+":"+myCourse.getTitle()+"]");
		}
		return created;
	}

	/**
	 * 
	 * @param data
	 * @param myPresentation
	 * @param teacherId
	 * @param sessions
	 * @param groups
	 * @return
	 */
	protected boolean validComponent(XcriOxcapPopulatorInstanceData data, 
			CourseComponentDAO myPresentation,
			String teacherId, 
			Set<Session> sessions, 
			CourseGroupDAO group) {

		int i=0;

		try {

			if (null != myPresentation.getOpens() && null != myPresentation.getCloses()) {
				if (myPresentation.getOpens().after(myPresentation.getCloses())){
					logMe(data, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] Open date is after close date");
					i++;
				}
			}

			if (myPresentation.getTitle() == null || myPresentation.getTitle().trim().length() == 0) {
				logMe(data, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] Title isn't set");
				i++;
			}

			if (null == group) {
				logMe(data, "Log Failure Teaching Instance ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"] No Assessment Unit codes");
				i++;
			}

			if (i == 0) {
				return true;
			}

		} catch (IOException e) {

		}
		return false;
	}

	/**
	 * 
	 * @param data
	 * @param myPresentation
	 * @param teacherId
	 * @param sessions
	 * @param groups
	 * @return
	 * @throws IOException
	 */
	private boolean updateComponent(XcriOxcapPopulatorInstanceData data, 
			CourseComponentDAO myPresentation,
			String teacherId,
			Set<Session> sessions, CourseGroupDAO group) throws IOException {

		boolean created = false;
		if (null != dao) {
			CourseComponentDAO componentDao = dao.findCourseComponent(myPresentation.getPresentationId());
			if (componentDao == null) {
				componentDao = dao.newCourseComponent(myPresentation.getPresentationId());
				created = true;
			}
			componentDao.setTitle(myPresentation.getTitle());
			componentDao.setSubject(myPresentation.getSubject());
			componentDao.setOpens(myPresentation.getOpens());
			componentDao.setOpensText(myPresentation.getOpensText());
			componentDao.setCloses(myPresentation.getCloses());
			componentDao.setClosesText(myPresentation.getClosesText());
			componentDao.setStarts(myPresentation.getStarts());
			componentDao.setStartsText(myPresentation.getStartsText());
			componentDao.setEnds(myPresentation.getEnds());
			componentDao.setEndsText(myPresentation.getEndsText());
			componentDao.setBookable(myPresentation.isBookable());
			componentDao.setSize(myPresentation.getSize());
			componentDao.setTermcode(myPresentation.getTermcode());
			componentDao.setAttendanceMode(myPresentation.getAttendanceMode());
			componentDao.setAttendanceModeText(myPresentation.getAttendanceModeText());
			componentDao.setAttendancePattern(myPresentation.getAttendancePattern());
			componentDao.setAttendancePatternText(myPresentation.getAttendancePatternText());
			componentDao.setComponentId(myPresentation.getComponentId()+":"+myPresentation.getTermcode());
			componentDao.setTeacherName(myPresentation.getTeacherName());
			componentDao.setTeacherEmail(myPresentation.getTeacherEmail());
			componentDao.setWhen(myPresentation.getWhen());
			componentDao.setSlot(myPresentation.getSlot());
			componentDao.setSessions(myPresentation.getSessions());
			componentDao.setLocation(myPresentation.getLocation());
			componentDao.setApplyTo(myPresentation.getApplyTo());
			componentDao.setMemberApplyTo(myPresentation.getMemberApplyTo());
			componentDao.setTeachingDetails(myPresentation.getTeachingDetails());

			componentDao.setBaseDate(baseDate(componentDao));
			componentDao.setSource(myPresentation.getSource());

			// Populate teacher details.
			// Look for details in WebLearn first then fallback to details in DAISY.
			if (teacherId != null && teacherId.length() > 0) {
				UserProxy teacher = proxy.findUserByEid(teacherId);
				if (teacher != null) {
					componentDao.setTeacherName(teacher.getDisplayName());
					componentDao.setTeacherEmail(teacher.getEmail());
				}
			}

			// Use of Set filters duplicates
			componentDao.getGroups().add(group);
			componentDao.setDeleted(false);

			Collection<CourseComponentSessionDAO> componentSessions = componentDao.getComponentSessions();
			for (Session session : sessions) {
				componentSessions.add(
						new CourseComponentSessionDAO(session.getIdentifiers()[0].getValue(),
								session.getStart().getDtf(), session.getStart().getValue(), 
								session.getEnd().getDtf(), session.getEnd().getValue()));
			}


			dao.save(componentDao);
		}

		if (created) {
			logMs(data, "Log Success Course Component created ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
			data.incrComponentCreated();
		} else {
			logMs(data, "Log Success Course Component updated ["+myPresentation.getPresentationId()+":"+myPresentation.getTitle()+"]");
			data.incrComponentUpdated();
		}
		return created;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void logMe(XcriOxcapPopulatorInstanceData data, String message) throws IOException {
		log.warn(message);
		if (null != data) {
			data.logMe(message);
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void logMs(XcriOxcapPopulatorInstanceData data, String message) throws IOException {
		log.warn(message);
		if (null != data) {
			data.logMs(message);
		}
	}


	/**
	 * 
	 * @param userCode
	 * @return
	 */
	private String getUser (String userCode) {

		if (null == proxy) {
			return userCode;
		}
		UserProxy user = proxy.findUserByEid(userCode);
		if (null == user) {
			log.warn("Failed to find User [" + userCode +"]");
			return null;
		}
		return user.getId();
	}

	protected static String viewDate(Date date, String text) {
		if (null == date) {
			return text+"[null]";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return text+"["+sdf.format(date)+"]";
	}

	private static boolean parseBoolean(String data) {
		if ("1".equals(data)) {
			return true;
		}
		if ("0".equals(data)) {
			return false;
		}
		return Boolean.parseBoolean(data);
	}

	/**
	 * 
	 * @param component
	 * @return
	 */
	public static Date baseDate(CourseComponentDAO component) {
		if (null != component.getStarts()) {
			return component.getStarts();
		}
		if (null != component.getCloses()) {
			return component.getCloses();
		}
		return null;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	protected static String parse(String data) {

		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll(">", "&gt;");
		data = FormattedText.convertPlaintextToFormattedText(data);

		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(data);

		StringBuffer sb = new StringBuffer(data.length());
		while (matcher.find()) {
			String text = matcher.group(0);
			matcher.appendReplacement(sb, "<a class=\"email\" href=\"mailto:"+text+"\">"+text+"</a>" );
		}
		matcher.appendTail(sb);

		pattern = Pattern.compile("(https?|ftps?):\\/\\/[a-z_0-9\\\\\\-]+(\\.([\\w#!:?+=&%@!\\-\\/])+)+", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sb.toString());

		sb = new StringBuffer(data.length());
		while (matcher.find()) {
			String text = matcher.group(0);
			matcher.appendReplacement(sb, "<a class=\"url\" href=\""+text+"\" target=\"_blank\">"+text+"</a>" );
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}
