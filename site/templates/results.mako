<%inherit file="base.mako" />

<%namespace name="search" file="searchbox.mako" />

<%def name="body()">
    <!--todo prepopulate box with old query -->
    ${search.box()}
    % for result, score in results:
    <div class="result">
        <a class="result_title" href="/tune/${result.id}">${result.title}</a>
        <div class="result_snippet">${result.abcsnippet}</div>
    </div>
    % endfor
    <script type="text/javascript">
        $(document).ready(function() {
            $(".result_snippet").each(function (i) {
                var abc = $(this).text();
                $(this).text("");
                var parser = new AbcParse({});
                parser.parse(abc);
                var tune = parser.getTune();
	            var paper = Raphael(this, 600, 40);
	            var printer = new ABCPrinter(paper, {});
                printer.printABC(tune);
            });
        });
    </script>
</%def>
