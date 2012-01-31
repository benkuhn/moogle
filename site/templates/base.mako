<html>
	<head>
		<link rel="stylesheet" type="text/css" href="/css/style.css" />
		<title>${title}</title>
		<script src="/js/abcjs_basic_1.0.5-min.js" type="text/javascript"> </script>
		<script src="/js/jquery-1.5.1.js" type="text/javascript"> </script>
	</head>
	<body>
		<div id="header">
			<a id="site-name" href="/">Moogle</a>
			<div id="quicksearch">
			<form action="/search" style="display:inline">
				<input type="text" name="search" id="search" placeholder="type a melody">
				<button id="searchButton" type="submit" name="quicksearch">Search</button>
			</form><br>
			<a id="what" href="/faq" style="font-style:italic;font-size:9pt;margin-left:20px">wait, what's going on?</a>
			</div>
			<ul id="nav">
			% for link in []:#nav_links:
				% if link == nav_current:
				<li class="nav_elt current">
				% else:
				<li class="nav_elt">
				% endif
					<a class="nav_link" href="${link.href}">${link.text}</a>
				</li>
			% endfor
			</ul>
		</div>
		<div id="body">
			${self.body()}
		</div>
		<div id="sidebar"></div>
	</body>
</html>
