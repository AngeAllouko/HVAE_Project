<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" import="com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<h1>Database not available</h1>
<div class="alert error">HVAE could not connect to or initialise MySQL.<br><code><%= Html.esc(request.getAttribute("dbError")) %></code></div>
<div class="card">
  <h3>Checklist</h3>
  <ol>
    <li>Start the MySQL server.</li>
    <li>Set the correct user and password in <code>src/main/resources/db.properties</code> (or the environment variables
      <code>HVAE_DB_URL</code>, <code>HVAE_DB_USER</code>, <code>HVAE_DB_PASSWORD</code>) and redeploy.</li>
    <li>Make sure that user may create the <code>hvae</code> database and tables.</li>
  </ol>
  <p><a class="button" href="<%= ctx %>/home?retry=1">Retry connection</a></p>
</div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
