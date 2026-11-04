<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.hvae.db.EvaluationDao,com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<%
    @SuppressWarnings("unchecked") List<EvaluationDao.Summary> items = (List<EvaluationDao.Summary>) request.getAttribute("items");
%>
<h1>Evaluation history</h1>
<% if (items.isEmpty()) { %>
<p>No evaluations yet. <a href="<%= ctx %>/home">Evaluate a response</a>.</p>
<% } else { %>
<div class="scroll"><table>
  <thead><tr><th>#</th><th>When</th><th>Scenario</th><th>Response</th><th>Overall</th></tr></thead>
  <tbody>
  <% for (EvaluationDao.Summary s : items) { %>
  <tr>
    <td><a href="<%= ctx %>/evaluation?id=<%= s.id() %>"><%= s.id() %></a></td>
    <td><%= Html.esc(s.createdAt()) %></td>
    <td><%= Html.esc(s.scenarioTitle()) %></td>
    <td><%= Html.esc(s.responseExcerpt()) %></td>
    <td><%= Html.badgeByName(s.overall()) %></td>
  </tr>
  <% } %>
  </tbody>
</table></div>
<% } %>
<%@ include file="/WEB-INF/views/footer.jspf" %>
