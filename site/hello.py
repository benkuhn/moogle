import cherrypy, os, process, model, re
from mako.template import Template
from mako.lookup import TemplateLookup

DEBUG = True

current_dir = os.path.dirname(os.path.abspath(__file__))
config = {'global': {'server.socket_host': '0.0.0.0'},
          '/': {'tools.staticdir.root': current_dir + '/static',
                'tools.sessions.on': True,
                'tools.sessions.timeout': 60},
          '/css': {'tools.staticdir.on': True, 'tools.staticdir.dir': 'css'},
          '/js': {'tools.staticdir.on': True, 'tools.staticdir.dir': 'js'}
          }

faq = [
("What's all this, then?",
"""This is what I did for my project week at <a
href="http://commschool.org">Commonwealth</a>. It's a database of
music where you can search for a melody and get back tunes that sound
sort of like it."""),
("How do I use it?",
"""Type in a string of ABC notation, and then click the "search"
button. There's more info on the main page.
"""),
("The song I'm looking for isn't coming up.",
"""There's not much music freely available in computer-readable
format. The song you're looking for probably isn't in the database.
"""),
("How does this work?",
"""The front-end site is built in Python using <a
href="http://cherrypy.org/">CherryPy</a>. Behind the scenes, it
communicates with a database of music that's written in Java. I had to
write the database myself because there weren't any databases that
could do this kind of fuzzy matching (with no word boundaries) in the
database, and transferring the data to another process to compute with
it was prohibitively slow.
"""),
("What sort of algorithm are you using?",
"""It's an adaptation of an algorithm from bioinformatics called <a
href="http://blast.ncbi.nlm.nih.gov/Blast.cgi">BLAST</a>. I had to
re-design the scoring step to make it run fast enough for a Web site.
"""),
("I want to know more.",
"""<a href="https://github.com/benskuhn/moogle">Read the source.</a>""")
]
abcre = re.compile(r"[a-gA-G,'^_=]+")
lookup = TemplateLookup(directories=['templates/'], default_filters=['decode.utf8'],
        input_encoding='utf-8', output_encoding='utf-8')

class MainPage:
    @cherrypy.expose
    def index(self):
        return lookup.get_template('search.mako').render(title="Moogle - a search engine for music")
    
    @cherrypy.expose
    def search(self, **kwargs):
        words = []
        nums = []
        if "quicksearch" in kwargs:
            for word in kwargs["search"].split():
                if abcre.match(word).end() != len(word):
                    words.append(word)
                else:
                    notes = process.parse(word)[0]
                    nums += notes
            # for now, we discard words and search for nums
        elif "search" in kwargs:
            nums = process.parse(kwargs["abc"])[0]
        else:
            return lookup.get_template('search.mako').render(title="Search Moogle")
        if len(nums) < 5:
            return lookup.get_template('error.mako').render(title="Error", text=
                """You must search for a melody at least 5 notes long, and preferably a fair bit longer.""")
        results = []
        for x in process.search(nums).split()[:10]:
            i, score = x.split(":")[:2]
            tune = process.single_tune(i)
            results.append((tune, score))
        return lookup.get_template('results.mako').render(title="Results from Moogle",
            results = results)
    @cherrypy.expose
    def tune(self, num):
        obj = model.session.query(model.Tune).filter(model.Tune.id == num).first()
        print obj
        return lookup.get_template('tune.mako').render(tune=obj, title="Moogle - " + obj.title)
    @cherrypy.expose
    def faq(self):
        return lookup.get_template('faq.mako').render(faq=faq, title="Moogle - FAQ")
cherrypy.quickstart(MainPage(), "/", config=config)
