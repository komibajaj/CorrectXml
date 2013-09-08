package correctxml

class CleanXmlService {

	def gpHtmlParser = new GpHtmlParser()

    def cleanThis(def inputXml) {
    
    	def outputXml = gpHtmlParser.reachedHere(inputXml)
    	
    }
}
