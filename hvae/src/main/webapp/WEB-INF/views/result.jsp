<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.hvae.model.*,com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<%
    EvaluationResult r = (EvaluationResult) request.getAttribute("result");
    Scenario sc = r.getScenario();
    // Show the most severe values first so violations are visible without scrolling.
    List<ValueAssessment> sorted = new ArrayList<>(r.getAssessmentList());
    sorted.sort((x, y) -> Integer.compare(y.getOutcome().getSeverity(), x.getOutcome().getSeverity()));
    String when = r.getCreatedAt() == null ? "" : r.getCreatedAt().toLocalDateTime().withNano(0).toString().replace('T', ' ');
%>
<h1>Evaluation #<%= r.getId() %></h1>
<div class="summary card">
  <div class="overall">Overall result: <%= Html.badge(r.getOverall()) %></div>
  <div class="strip" aria-label="Outcome per value">
    <% for (ValueAssessment a : sorted) { %>
    <a class="strip-item" href="#v-<%= a.getValue().name().toLowerCase() %>"><i class="dot v-<%= a.getValue().name().toLowerCase() %>"></i><%= Html.esc(a.getValue().getLabel()) %> <%= Html.badge(a.getOutcome()) %></a>
    <% } %>
  </div>
  <p class="hint">The overall result is the most severe outcome among the assessed values
    (misaligned &gt; partially aligned &gt; insufficient information &gt; aligned).</p>
  <dl>
    <dt>Scenario</dt><dd><%= Html.esc(sc.getTitle()) %></dd>
    <dt>User request</dt><dd><%= Html.esc(sc.getPrompt()) %></dd>
    <% if (sc.getKnownFacts() != null) { %><dt>Reference fact</dt><dd><%= Html.esc(sc.getKnownFacts()) %></dd><% } %>
    <dt>Evaluated</dt><dd><%= Html.esc(when) %></dd>
  </dl>
  <h3>Candidate response</h3>
  <blockquote class="response"><%= Html.esc(r.getResponse()) %></blockquote>
</div>

<% for (ValueAssessment a : sorted) { %>
<section class="card value" id="v-<%= a.getValue().name().toLowerCase() %>">
  <div class="vhead">
    <h2><i class="dot v-<%= a.getValue().name().toLowerCase() %>"></i> <%= Html.esc(a.getValue().getLabel()) %></h2>
    <div><%= Html.badge(a.getOutcome()) %> <span class="score">score <%= a.getScore() %></span></div>
  </div>
  <p class="note"><%= Html.esc(a.getNote()) %></p>
  <% if (!a.getTriggered().isEmpty()) { %>
  <div class="scroll">
  <table>
    <thead><tr><th>Rule</th><th>Weight</th><th>Matched text</th><th>Why it matters</th><th>Recommendation</th></tr></thead>
    <tbody>
    <% for (TriggeredRule t : a.getTriggered()) { Rule ru = t.getRule(); %>
    <tr class="<%= t.isSuppressed() ? "suppressed" : "" %>">
      <td><strong><%= Html.esc(ru.getId()) %></strong><br><%= Html.esc(ru.getName()) %>
        <% if (t.isSuppressed()) { %><br><em>Not counted: negation/warning cue &ldquo;<%= Html.esc(t.getSuppressedBy()) %>&rdquo; found before the match.</em><% } %></td>
      <td><%= ru.getWeight() %></td>
      <td><code><%= Html.esc(t.getMatchedText()) %></code></td>
      <td><%= Html.esc(ru.getExplanation()) %></td>
      <td><%= Html.esc(ru.getRecommendation()) %></td>
    </tr>
    <% } %>
    </tbody>
  </table>
  </div>
  <% } %>
</section>
<% } %>
<p><a class="button" href="<%= ctx %>/home">Evaluate another response</a> <a class="button secondary" href="<%= ctx %>/history">History</a></p>
<%@ include file="/WEB-INF/views/footer.jspf" %>
