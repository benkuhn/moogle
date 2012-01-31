<%def name="box()">
	<div id="slowsearch"><form action="/search" style="float:left">
	    <textarea name="abc" id="abc"></textarea>
	    <button id="search-button" name="search" type="submit">Search</button>
	</form></div>
    <div id="abchelp" class="inline">
        <div>You should enter your search in <i>ABC notation</i>. It's pretty
        simple. Examples:</div>
        <table style="margin-left:auto;margin-right:auto"> <tr>
            <td class="ex">C</td><td class="trans">middle C</td>
            <td class="ex">^C</td><td class="trans">middle C sharp</td>
            <td class="ex">_C</td><td class="trans">middle C flat</td>
        </tr><tr>
            <td class="ex">C,</td><td class="trans">low C</td>
            <td class="ex">c</td><td class="trans">(lowercase) high C</td>
            <td class="ex">c'</td><td class="trans">really high C</td>
        </tr>
        </table>
        <div>A preview will come up once you start typing.
        </div>
    </div>
	<div id="abcrendered">
	</div>
	<script type="text/javascript">
	    var paper = Raphael(document.getElementById("abcrendered"), 600, 80);
	    var printer = new ABCPrinter(paper, {});
	    $("#abc").keyup(function () {
	        var abc = "L:1/4\n" + $("#abc").val();
	        var parser = new AbcParse({});
	        parser.parse(abc);
	        var tune = parser.getTune();
	        paper.clear();
	        printer.printABC(tune);
	    });
	</script>
</%def>
