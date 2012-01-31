<%inherit file="base.mako" />

<%namespace name="search" file="searchbox.mako" />

<%def name="body()">
    ${search.box()}
</%def>
