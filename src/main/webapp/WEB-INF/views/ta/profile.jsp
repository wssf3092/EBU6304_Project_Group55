<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="two-column">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Details</h2>
            </div>
            <c:choose>
                <c:when test="${profile.complete}">
                    <span class="badge badge-success">Profile complete</span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-warn">Incomplete</span>
                </c:otherwise>
            </c:choose>
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
                <span>Year of study</span>
                <input type="number" min="1" max="8" name="year" value="<c:out value='${profile.year}'/>" required/>
            </label>
            <label class="field span-two">
                <span>Skills <small>(comma-separated, e.g. Java, Python, Marking)</small></span>
                <textarea name="skills" placeholder="Java, Python, Teaching Support, Communication" required><c:forEach items="${profile.skills}" var="skill" varStatus="state"><c:out value="${skill}"/><c:if test="${not state.last}">, </c:if></c:forEach></textarea>
            </label>
            <label class="field span-two">
                <span>Summary <small>(20–500 characters)</small></span>
                <textarea name="bio" maxlength="500" required><c:out value="${profile.bio}"/></textarea>
                <small class="char-hint"><span id="bioLen"><c:out value="${fn:length(profile.bio)}"/></span> / 500</small>
            </label>
            <label class="field">
                <span>Weekly workload limit (h)</span>
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
            <c:choose>
                <c:when test="${profile.hasCv}">
                    <span class="badge badge-success">Uploaded</span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-warn">Not uploaded</span>
                </c:otherwise>
            </c:choose>
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
                <span>Upload PDF or DOCX <small>(max 5 MB)</small></span>
                <input type="file" name="cvFile" accept=".pdf,.docx" required/>
            </label>
            <button class="btn primary wide" type="submit">Upload CV</button>
        </form>
        <c:if test="${profile.hasCv}">
            <div class="panel-actions">
                <a class="btn secondary" href="${pageContext.request.contextPath}/files/cv?userId=${currentUser.userId}">Download my CV</a>
            </div>
        </c:if>
    </section>
</div>
<script>
    (function () {
        var bio = document.querySelector('textarea[name="bio"]');
        var counter = document.getElementById('bioLen');
        if (bio && counter) {
            bio.addEventListener('input', function () {
                counter.textContent = bio.value.length;
            });
        }
    })();
</script>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
