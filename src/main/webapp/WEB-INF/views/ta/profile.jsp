<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="two-column">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Details</h2>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/ta/profile" class="form-grid two">
            <label class="field">
                <span>Full name</span>
                <input type="text" value="<c:out value='${currentUser.name}'/>" disabled/>
            </label>
            <label class="field">
                <span>Student ID</span>
                <input type="text" name="studentId" value="<c:out value='${profile.studentId}'/>" required/>
            </label>
            <label class="field">
                <span>Account email</span>
                <input type="email" value="<c:out value='${currentUser.email}'/>" disabled/>
            </label>
            <label class="field">
                <span>Contact email</span>
                <input type="email" name="contactEmail" value="<c:out value='${profile.contactEmail}'/>" required/>
            </label>
            <label class="field">
                <span>Major</span>
                <input type="text" name="major" value="<c:out value='${profile.major}'/>" required/>
            </label>
            <label class="field">
                <span>Year</span>
                <input type="number" min="1" max="8" name="year" value="<c:out value='${profile.year}'/>" required/>
            </label>
            <label class="field span-two">
                <span>Skills</span>
                <textarea name="skills" placeholder="Java, Python, Teaching Support, Communication" required><c:forEach items="${profile.skills}" var="skill" varStatus="state"><c:out value="${skill}"/><c:if test="${not state.last}">, </c:if></c:forEach></textarea>
            </label>
            <label class="field span-two">
                <span>Summary</span>
                <textarea name="bio" required><c:out value="${profile.bio}"/></textarea>
            </label>
            <label class="field">
                <span>Weekly workload limit</span>
                <input type="number" min="1" max="40" name="maxHours" value="<c:out value='${profile.maxWorkloadHoursPerWeek}'/>" required/>
            </label>
            <div class="field action-field">
                <button class="btn primary wide" type="submit">Save Changes</button>
            </div>
        </form>
    </section>

    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>CV</h2>
            </div>
        </div>
        <div class="detail-stack">
            <div class="stat-line">
                <span>File</span>
                <strong><c:out value="${empty profile.cvFileName ? 'None' : profile.cvFileName}"/></strong>
            </div>
            <div class="stat-line">
                <span>Updated</span>
                <strong><c:out value="${profile.displayCvUploadedAt}"/></strong>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/ta/cv/upload" enctype="multipart/form-data" class="upload-box">
            <label class="field">
                <span>Upload PDF or DOCX</span>
                <input type="file" name="cvFile" accept=".pdf,.docx" required/>
            </label>
            <button class="btn primary wide" type="submit">Upload CV</button>
        </form>
    </section>
</div>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
