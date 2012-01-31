<%inherit file="base.mako" />

<%def name="body()">
% for f in faq:
    <p class="faq">${f[0]}</p>
    <p class="faa">${f[1]}</b>
    <br>
% endfor
</%def>
