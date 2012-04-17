/**
 * ASE Spotter.
 * See http://en.wikipedia.org/wiki/Wikipedia:ASE
 * Author: Nicolas Raoul
 */

/* ******************************** Configuration ******************************** */

/**
 * Number of revisions to consider for an article.
 */
REVISIONS_LIMIT = 50

/**
 * Number of edits after which an editor is considered experienced.
 */
EXPERIENCED_EDITCOUNT = 900

/**
 * Interval to wait between 2 requests to the server, in milliseconds.
 */
REQUESTS_INTERVAL = 300

/* ******************************** Main script ******************************** */

/**
 * Main.
 */
System.setProperty("http.agent", "WikiProject ASE")
spotAseArticles(10000)

/**
 * Spot ASE articles within the given number of articles.
 */
def spotAseArticles(numberOfArticles) {
        for ( i in 1..numberOfArticles ) {

                // Get random page name.
                def node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&list=random&rnnamespace=0&rnlimit=1&format=xml")
                def title = node.query.random.page.@title.toString()

                // Skip if disambiguation.
                if ( title.contains("disambiguation") ) {
                        println "Disambiguation: " + title
                        continue
                }

                // Get editors of this page.
                def urlencodedTitle = java.net.URLEncoder.encode(title)
                node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=" + urlencodedTitle + "&rvprop=user&rvlimit=" + REVISIONS_LIMIT + "&format=xml")
                def revisions = node.query.pages.page.revisions.rev
                println "Article with " + revisions.size() + " revisions: " + title
                def editors = [] as Set
                for ( revision in revisions ) {
                        def editor = revision.@user.toString()
                        if ( ! isBot(editor) ) {
                                editors.add(editor)
                        }
                }

                // Ignore articles created by a bot.
                // If the last editor in the list (which chronologically means the first editor) is a bot, then continue.
                // (only works for articles with less than 50 edits)
                def lastEditor = revisions.list().last().@user.toString()
                if ( revisions.list().size() < REVISIONS_LIMIT && isBot(lastEditor) ) {
                        println "  Creator is a bot: " + lastEditor
                        continue
                }

                if ( editors.size() != 0 ) {

                        // Check whether at least an experienced editor has edited the page
                        def atLeastOneExperiencedEditor = false
                        for( editor in editors ) {
                                // Wait a bit to reduce load on the local host and on the server.
                                Thread.sleep(REQUESTS_INTERVAL)
                                node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&list=users&ususers=" + editor + "&usprop=editcount&format=xml")
                                //println "                [DEBUG] editor=" + editor + ", editcount=" + node.query.users.user.@editcount.toString()
                                def editcountString = node.query.users.user.@editcount.toString()
                                def editcount = 0;
                                if ( editcountString.length() > 0) {
                                        editcount = Integer.parseInt(node.query.users.user.@editcount.toString())
                                }
                                if ( editcount > EXPERIENCED_EDITCOUNT ) {
                                        println "  Editor experienced: " + editor + " (editcount:" + editcount + ")"
                                        atLeastOneExperiencedEditor = true
                                        break
                                }
                                else {
                                        println "  Editor unexperienced: " + editor + " (editcount:" + editcount + ")"
                                }
                        }

                        if ( atLeastOneExperiencedEditor == false ) {
                                spot(title)
                        }
                }
        }
}

def spot (title) {
        println "* [[" + title + "]]"
}

def isBot (editorName) {
        return editorName.toLowerCase().contains("bot")
}
