<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
    import="java.util.*,com.hvae.model.*,com.hvae.eval.*,com.hvae.db.TestCaseDao,com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<%
    @SuppressWarnings("unchecked") List<TestCaseDao.RunSummary> runs = (List<TestCaseDao.RunSummary>) request.getAttribute("runs");
    @SuppressWarnings("unchecked") Map<String, Integer> splitCounts = (Map<String, Integer>) request.getAttribute("splitCounts");
    TestCaseDao.RunSummary run = (TestCaseDao.RunSummary) request.getAttribute("run");
    Report rep = (Report) request.getAttribute("report");
%>
<h1>Testing and results</h1>
<p class="lead">Runs the labelled test cases through the rule engine and compares predictions with the reference labels.
A case counts as a <em>flagged violation</em> if the outcome is partially aligned or misaligned.</p>

<div class="alert warn">
  <strong>Report only measured results.</strong> The bundled dataset is a <em>draft</em> written together with the rules. Before reporting
  numbers in a paper, have the reference labels reviewed independently by a second person, and keep the TEST split for final
  measurement only (write and refine rules using the DEV split).
</div>

<div class="card">
  <p>Dataset:
  <% for (Map.Entry<String, Integer> e : splitCounts.entrySet()) { %><strong><%= Html.esc(e.getKey()) %></strong>: <%= e.getValue() %> cases &nbsp;<% } %></p>
  <form method="post" action="<%= ctx %>/testing" class="inline">
    <button name="split" value="DEV"  class="secondary">Run DEV split</button>
    <button name="split" value="TEST" class="primary">Run TEST split</button>
    <button name="split" value="ALL"  class="secondary">Run all</button>
  </form>
</div>

<% if (rep != null && run != null) { %>
<h2>Run #<%= run.id() %> &ndash; <%= Html.esc(run.split()) %> split, <%= run.nCases() %> cases, &tau; = <%= run.tau() %></h2>
<div class="scroll"><table class="metrics">
  <thead><tr><th>Value</th><th>TP</th><th>FP</th><th>TN</th><th>FN</th><th>Accuracy</th><th>Precision</th><th>Recall</th><th>F1</th></tr></thead>
  <tbody>
  <% for (Value v : Value.values()) { Confusion c = rep.getPerValue().get(v); %>
  <tr><td><%= Html.esc(v.getLabel()) %></td><td><%= c.getTp() %></td><td><%= c.getFp() %></td><td><%= c.getTn() %></td><td><%= c.getFn() %></td>
      <td><%= c.getAccuracyText() %></td><td><%= c.getPrecisionText() %></td><td><%= c.getRecallText() %></td><td><%= c.getF1Text() %></td></tr>
  <% } %>
  <tr class="total"><td>Macro average</td><td colspan="4"></td><td><%= rep.getMacroAccuracyText() %></td><td><%= rep.getMacroPrecisionText() %></td>
      <td><%= rep.getMacroRecallText() %></td><td><%= rep.getMacroF1Text() %></td></tr>
  </tbody>
</table></div>
<p class="hint">&ldquo;n/a&rdquo; means the metric is undefined (zero denominator). Exact three-class agreement with the reference labels:
  <%= rep.getExactMatchText() %>. Insufficient-information predictions: <%= rep.getOverall().getPredictedInsufficient() %>
  (reference: <%= rep.getOverall().getReferenceInsufficient() %>); these count as &ldquo;not flagged&rdquo; in the binary metrics.</p>
<p><a class="button secondary" href="<%= ctx %>/export?run=<%= run.id() %>">Download run as CSV</a></p>

<h2>Error analysis: misclassified cases</h2>
<% List<CaseResult> errs = rep.getErrors(); if (errs.isEmpty()) { %>
<p>No false positives or false negatives in this run.</p>
<% } else { %>
<div class="scroll"><table>
  <thead><tr><th>Case</th><th>Value</th><th>Type</th><th>Reference</th><th>Predicted</th><th>Rules fired</th><th>Response</th></tr></thead>
  <tbody>
  <% for (CaseResult e : errs) { %>
  <tr><td><%= Html.esc(e.getCaseId()) %></td><td><%= Html.esc(e.getValue().getLabel()) %></td>
      <td><span class="tag <%= e.getErrorType().toLowerCase() %>"><%= e.getErrorType() %></span></td>
      <td><%= Html.badge(e.getReference()) %></td><td><%= Html.badge(e.getPredicted()) %></td>
      <td><%= Html.esc(e.getRuleIds().isEmpty() ? "none" : e.getRuleIds()) %></td><td><%= Html.esc(e.getResponse()) %></td></tr>
  <% } %>
  </tbody>
</table></div>
<% } %>
<% } %>

<% if (!runs.isEmpty()) { %>
<h2>Previous runs</h2>
<div class="scroll"><table>
  <thead><tr><th>Run</th><th>When</th><th>Split</th><th>Cases</th><th>&tau;</th></tr></thead>
  <tbody>
  <% for (TestCaseDao.RunSummary rs : runs) { %>
  <tr><td><a href="<%= ctx %>/testing?run=<%= rs.id() %>">#<%= rs.id() %></a></td><td><%= Html.esc(rs.runAt()) %></td>
      <td><%= Html.esc(rs.split()) %></td><td><%= rs.nCases() %></td><td><%= rs.tau() %></td></tr>
  <% } %>
  </tbody>
</table></div>
<% } %>
<%@ include file="/WEB-INF/views/footer.jspf" %>
