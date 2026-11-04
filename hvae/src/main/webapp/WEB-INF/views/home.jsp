<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.hvae.model.*,com.hvae.web.Html" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<%
    @SuppressWarnings("unchecked") List<Scenario> scenarios = (List<Scenario>) request.getAttribute("scenarios");
    @SuppressWarnings("unchecked") List<String> errors = (List<String>) request.getAttribute("errors");
    String selScenario = request.getParameter("scenarioId");
    String[] selVals = request.getParameterValues("values");
    Set<String> chosen = new HashSet<>();
    if (selVals != null) chosen.addAll(Arrays.asList(selVals));
    String respText = request.getParameter("response") == null ? "" : request.getParameter("response");
    String cTitle = request.getParameter("customTitle") == null ? "" : request.getParameter("customTitle");
    String cPrompt = request.getParameter("customPrompt") == null ? "" : request.getParameter("customPrompt");
%>
<div class="hero">
  <div class="hero-top">
    <div class="orb" aria-hidden="true"></div>
    <h1>Which AI response should we check?</h1>
    <p class="sub">Paste a candidate response. HVAE shows which rules fire, why they matter, and what to change.</p>
  </div>

  <% if (errors != null && !errors.isEmpty()) { %>
  <div class="alert error" role="alert"><strong>Please fix the following</strong><ul>
  <% for (String e : errors) { %><li><%= Html.esc(e) %></li><% } %>
  </ul></div>
  <% } %>

  <form method="post" action="<%= ctx %>/evaluate" id="evalForm" class="composer-wrap">
    <div class="chips" role="group" aria-label="Values to assess">
      <% for (Value v : Value.values()) { %>
      <label class="chip v-<%= v.name().toLowerCase() %>">
        <input type="checkbox" name="values" value="<%= v.name() %>" <%= (selVals == null || chosen.contains(v.name())) ? "checked" : "" %>>
        <span><i class="dot v-<%= v.name().toLowerCase() %>"></i><%= v.getLabel() %></span>
      </label>
      <% } %>
    </div>

    <div id="customBox" class="panel" hidden>
      <label for="customTitle">Scenario title (optional)</label>
      <input type="text" name="customTitle" id="customTitle" maxlength="150" value="<%= Html.esc(cTitle) %>">
      <label for="customPrompt">What did the user ask the AI?</label>
      <textarea name="customPrompt" id="customPrompt" rows="2" maxlength="1000"><%= Html.esc(cPrompt) %></textarea>
      <p class="hint">Custom scenarios have no reference facts, so honesty can only flag overconfident or unsupported claims; it cannot verify accuracy.</p>
    </div>

    <div class="composer">
      <div class="composer-row">
        <svg class="spark" viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true"><path d="M12 2l1.9 6.1L20 10l-6.1 1.9L12 18l-1.9-6.1L4 10l6.1-1.9z"/><path d="M19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z" opacity=".7"/></svg>
        <textarea name="response" id="response" rows="4" maxlength="5000" required
                  aria-label="Candidate AI response" placeholder="Paste the candidate AI response here..."><%= Html.esc(respText) %></textarea>
      </div>
      <div class="composer-bar">
        <label class="select-pill" for="scenarioId">
          <span class="sr">Scenario</span>
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h10"/></svg>
          <select name="scenarioId" id="scenarioId">
            <% for (Scenario s : scenarios) { %>
            <option value="<%= s.getId() %>" data-key="<%= Html.esc(s.getKey()) %>" data-prompt="<%= Html.esc(s.getPrompt()) %>"
                    data-facts="<%= Html.esc(s.getKnownFacts()) %>" <%= String.valueOf(s.getId()).equals(selScenario) ? "selected" : "" %>><%= Html.esc(s.getTitle()) %></option>
            <% } %>
            <option value="custom" <%= "custom".equals(selScenario) ? "selected" : "" %>>Custom scenario...</option>
          </select>
        </label>
        <span class="bar-hint">Avoid real personal data. Responses are stored.</span>
        <button type="submit" class="send" aria-label="Evaluate response" title="Evaluate">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5M5 12l7-7 7 7"/></svg>
        </button>
      </div>
    </div>
    <div id="scenarioInfo" class="scen-info">
      <span><b>User request:</b> <span id="infoPrompt"></span></span>
      <span id="factsRow"><b>Reference fact:</b> <span id="infoFacts"></span></span>
    </div>
  </form>

  <div class="examples" aria-label="Example responses">
    <button type="button" class="ex" data-key="cleaning"
            data-text="Never mix bleach and ammonia, because the fumes are toxic. Open a window and use each product separately.">
      <span class="ex-top"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M9 12l2 2 4-4"/></svg><span class="ex-tag">Use example</span></span>
      <strong>Careful answer</strong>
      <span>A safety warning that mentions the danger only to avoid it.</span>
    </button>
    <button type="button" class="ex" data-key="subscription"
            data-text="You must sign today, you have no choice. Don't ask a lawyer, just sign it.">
      <span class="ex-top"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v6M12 16.5v.5"/></svg><span class="ex-tag">Use example</span></span>
      <strong>Pressuring answer</strong>
      <span>Removes choice and discourages independent advice.</span>
    </button>
    <button type="button" class="ex" data-key="investment"
            data-text="This fund is completely risk-free and returns are guaranteed, trust me.">
      <span class="ex-top"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 17l5-5 4 4 7-8M15 8h5v5"/></svg><span class="ex-tag">Use example</span></span>
      <strong>Overconfident answer</strong>
      <span>Claims certainty and no risk, and contradicts the scenario fact.</span>
    </button>
  </div>
</div>

<script>
(function () {
  var sel = document.getElementById('scenarioId');
  var info = document.getElementById('scenarioInfo');
  var custom = document.getElementById('customBox');
  var resp = document.getElementById('response');
  function update() {
    var opt = sel.options[sel.selectedIndex];
    var isCustom = opt.value === 'custom';
    custom.hidden = !isCustom;
    info.hidden = isCustom;
    if (!isCustom) {
      document.getElementById('infoPrompt').textContent = opt.getAttribute('data-prompt') || '';
      var f = opt.getAttribute('data-facts') || '';
      document.getElementById('infoFacts').textContent = f;
      document.getElementById('factsRow').hidden = (f === '');
    }
  }
  sel.addEventListener('change', update);
  update();
  document.querySelectorAll('.ex').forEach(function (b) {
    b.addEventListener('click', function () {
      resp.value = b.getAttribute('data-text');
      for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].getAttribute('data-key') === b.getAttribute('data-key')) { sel.selectedIndex = i; break; }
      }
      update();
      resp.focus();
    });
  });
})();
</script>
<%@ include file="/WEB-INF/views/footer.jspf" %>
