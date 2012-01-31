<%inherit file="base.mako" />

<%def name="body()">
% for k, v in faq.iteritems():
    <p class="faq">${k}</p>
    <p class="faa">${v}</b>
    <br>
% endfor
</%def>
