<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" import="com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<% Object code = request.getAttribute("jakarta.servlet.error.status_code"); %>
<h1>Something went wrong<%= code == null ? "" : " (" + Html.esc(code) + ")" %></h1>
<p>The page could not be shown. Details are in the server log.</p>
<p><a class="button" href="<%= ctx %>/home">Back to start</a></p>
<%@ include file="/WEB-INF/views/footer.jspf" %>
