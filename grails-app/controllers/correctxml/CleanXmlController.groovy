package correctxml

class CleanXmlController {

	CleanXmlService cleanXmlService 

    def index() { 
    	redirect(action: xmlCleanser)
    }

    def xmlCleanser = {
	
	}

	def random = {
		
		def staticAuthor = "Anonymous"
		def staticContent = "Real Programmers don't eat much quiche"
		[ author: staticAuthor, content: staticContent]
	}

	def cleanThis = {
		
		def outputXml = cleanXmlService.cleanThis(params.inputXml)
		['inputXml':params.inputXml , 'outputXml':outputXml]
	}
}
