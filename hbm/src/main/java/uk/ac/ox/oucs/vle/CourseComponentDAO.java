package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Hibernate
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
// Generated Aug 17, 2010 10:15:40 AM by Hibernate Tools 3.2.2.GA


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * CourseComponentDAO generated by hbm2java
 */
public class CourseComponentDAO  implements java.io.Serializable {


     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int muid;
	private String presentationId;
    private int version;
    private String title;
    private String subject;
    private String termcode;
    private Date opens;
    private String opensText;
    private Date closes;
    private String closesText;
    private Date starts;
    private String startsText;
    private Date ends;
    private String endsText;
    private Date created;
    private Date baseDate;
    private boolean bookable;
    private boolean deleted;
    private int size;
    private int taken;
    private String componentId;
    private String teacherName;
    private String teacherEmail;
    private String when;
    private String slot;
    private String sessions;
    private String location;
    private String applyTo;
    private String memberApplyTo;
    private String teachingDetails;
    private String attendanceMode;
    private String attendanceModeText;
    private String attendancePattern;
    private String attendancePatternText;
    private String source;
    private Set<CourseSignupDAO> signups = new HashSet<CourseSignupDAO>(0);
    private Set<CourseGroupDAO> groups = new HashSet<CourseGroupDAO>(0);
    private Set<CourseComponentSessionDAO> componentSessions = new HashSet<CourseComponentSessionDAO>(0);

    public CourseComponentDAO() {
    }

    public CourseComponentDAO(String presentationId, boolean bookable) {
        this.presentationId = presentationId;
        this.bookable = bookable;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + muid;
		return result;
	}


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        CourseComponentDAO dao = (CourseComponentDAO)obj;
        return getMuid() == dao.getMuid();
    }

    
    public int getMuid() {
        return this.muid;
    }
    
	public void setMuid(int muid) {
        this.muid = muid;
    }
    
    public String getPresentationId() {
        return this.presentationId;
    }
    
    public void setPresentationId(String presentationId) {
        this.presentationId = presentationId;
    }
    
    
    public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}


	public String getSubject() {
        return this.subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
	public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTermcode() {
        return this.termcode;
    }
    
    public void setTermcode(String termcode) {
        this.termcode = termcode;
    }
    
    
    public Date getOpens() {
        return this.opens;
    }
    
    public void setOpens(Date opens) {
        this.opens = opens;
    }
    
    
    public String getOpensText() {
        return this.opensText;
    }
    
    public void setOpensText(String opensText) {
        this.opensText = opensText;
    }
    
    
    public Date getCloses() {
        return this.closes;
    }
    
    public void setCloses(Date closes) {
        this.closes = closes;
    }
    
    
    public String getClosesText() {
        return this.closesText;
    }
    
    public void setClosesText(String closesText) {
        this.closesText = closesText;
    }
    
    
    public Date getStarts() {
        return this.starts;
    }
    public void setStarts(Date starts) {
        this.starts = starts;
    }
    
    
    public String getStartsText() {
        return this.startsText;
    }
    
    public void setStartsText(String startsText) {
        this.startsText = startsText;
    }
    
    
    public Date getEnds() {
        return this.ends;
    }
    public void setEnds(Date ends) {
        this.ends = ends;
    }
    
    
    public String getEndsText() {
        return this.endsText;
    }
    
    public void setEndsText(String endsText) {
        this.endsText = endsText;
    }
    
    
    public Date getCreated() {
        return this.created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    
    
    public Date getBaseDate() {
        return this.baseDate;
    }
    public void setBaseDate(Date baseDate) {
        this.baseDate = baseDate;
    }
    
    
    public boolean isBookable() {
        return this.bookable;
    }
    
    public void setBookable(boolean bookable) {
        this.bookable = bookable;
    }
    
    
	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
    
    public int getSize() {
        return this.size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    public int getTaken() {
        return this.taken;
    }
    
    public void setTaken(int taken) {
        this.taken = taken;
    }
    public String getComponentId() {
        return this.componentId;
    }
    
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    public Set<CourseSignupDAO> getSignups() {
        return this.signups;
    }
    
    public void setSignups(Set<CourseSignupDAO> signups) {
        this.signups = signups;
    }
    public Set<CourseGroupDAO> getGroups() {
        return this.groups;
    }
    
    public void setGroups(Set<CourseGroupDAO> groups) {
        this.groups = groups;
    }

    
    public Set<CourseComponentSessionDAO> getComponentSessions() {
        return this.componentSessions;
    }
    
    public void setComponentSessions(Set<CourseComponentSessionDAO> componentSessions) {
        this.componentSessions = componentSessions;
    }

    
	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}


	public String getTeacherEmail() {
		return teacherEmail;
	}


	public void setTeacherEmail(String teacherEmail) {
		this.teacherEmail = teacherEmail;
	}


	public String getWhen() {
		return when;
	}


	public void setWhen(String when) {
		this.when = when;
	}


	public String getSlot() {
		return slot;
	}


	public void setSlot(String slot) {
		this.slot = slot;
	}


	public String getSessions() {
		return sessions;
	}

	public void setSessions(String sessions) {
		this.sessions = sessions;
	}


	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	

	public String getApplyTo() {
		return applyTo;
	}

	public void setApplyTo(String applyTo) {
		this.applyTo = applyTo;
	}
	
	
	public String getMemberApplyTo() {
		return memberApplyTo;
	}

	public void setMemberApplyTo(String memberApplyTo) {
		this.memberApplyTo = memberApplyTo;
	}
	
	
	public String getTeachingDetails() {
		return teachingDetails;
	}

	public void setTeachingDetails(String teachingDetails) {
		this.teachingDetails = teachingDetails;
	}
	
	
	public String getAttendanceMode() {
		return attendanceMode;
	}

	public void setAttendanceMode(String attendanceMode) {
		this.attendanceMode = attendanceMode;
	}
	
	public String getAttendanceModeText() {
		return attendanceModeText;
	}

	public void setAttendanceModeText(String attendanceModeText) {
		this.attendanceModeText = attendanceModeText;
	}

	public String getAttendancePattern() {
		return attendancePattern;
	}

	public void setAttendancePattern(String attendancePattern) {
		this.attendancePattern = attendancePattern;
	}
	
	public String getAttendancePatternText() {
		return attendancePatternText;
	}

	public void setAttendancePatternText(String attendancePatternText) {
		this.attendancePatternText = attendancePatternText;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}


