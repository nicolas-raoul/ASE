/**
 * Main program.
 * TODO:
 * Group random pages query
 * Group revisions query for several pages
 */
spotAseArticles(10000)

/**
 * Spot ASE articles within the given number of articles.
 */
def spotAseArticles(numberOfArticles) {
        for ( i in 1..numberOfArticles ) {

                // Get random page name
                def node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&list=random&rnnamespace=0&rnlimit=1&format=xml")
                def title = node.query.random.page.@title.toString()

                // Skip if disambiguation
                if ( title.contains("disambiguation") ) {
                        println "Disambiguation: " + title
                        continue
                }

                // Get editors of this page
                node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=" + title + "&rvprop=user&rvlimit=50&format=xml")
                def revisions = node.query.pages.page.revisions.rev
                println "Article with " + revisions.size() + " revisions: " + title
                def editors = [] as Set
                for ( revision in revisions ) {
                        def editor = revision.@user.toString()
                        if ( ! isBot(editor) ) {
                                editors.add(editor)
                        }
                }

                // Ignore articles created by a bot
                // If the last editor in the list (which chronologically means the first editor) is a bot, then continue
                // False positive might appear for articles with more than 50 edits, which is not a big loss.
                
                //def lastEditor = revisions.list().last().@user.toString()
                if ( ! revisions.list().isEmpty() && revisions.list().size() < 50 && isBot(/*lastEditor*/revisions.list().last().@user.toString()) ) {
                        println "  Creator is a bot: " /*+ lastEditor*/
                        continue
                }

                if ( editors.size() != 0 ) {

                        // Check whether at least an experienced editor has edited the page
                        def atLeastOneExperiencedEditor = false
                        for( editor in editors ) {
                                // Wait a bit to reduce load on the local host and on the server.
                                Thread.sleep(300)
                                node = new XmlSlurper().parse("http://en.wikipedia.org/w/api.php?action=query&list=users&ususers=" + editor + "&usprop=editcount&format=xml")
                                //println "                [DEBUG] editor=" + editor + ", editcount=" + node.query.users.user.@editcount.toString()
                                def editcountString = node.query.users.user.@editcount.toString()
                                def editcount = 0;
                                if ( editcountString.length() > 0) {
                                        editcount = Integer.parseInt(node.query.users.user.@editcount.toString())
                                }
                                if ( editcount > 900 ) {
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
