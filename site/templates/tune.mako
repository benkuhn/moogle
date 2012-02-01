<%inherit file="base.mako" />

<%def name="body()">
    <div id="tunetitle" style="text-align:center; font-size:30pt; width:800;">${tune.title}</div>
    <div id="tunerender"></div>
    <span style="font-weight:bold">ABC file</span>
    <pre id="tuneabc">${tune.abc}</pre>
    <script type="text/javascript">
        var abc = $("#tuneabc").text();
        var parser = new AbcParse({});
        parser.parse(abc);
        var tune = parser.getTune();
        var paper = Raphael(document.getElementById("tunerender"), 600, 80);
        var printer = new ABCPrinter(paper, {});
        paper.clear();
        printer.printABC(tune);
    </script>
</%def>
