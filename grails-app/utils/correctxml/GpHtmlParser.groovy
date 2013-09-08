package correctxml

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

class GpHtmlParser {

	def reachedHere(String inputXml){
		
		def retVal = "Cleaned " + fixQuestion(inputXml)
		
	}
	
	def getAllTags( String text ){
	//	List<String> tags = text.findAll("</?[A-Za-z][A-Za-z0-9]*(\\s+[a-zA-Z0-9]+=(\'|\")?\\w*-?:?%?(\'|\")?)*\\s*/?>" )
		List<String> tags  = text.findAll("<(.+?)/?>")
		/*tags.each {
			// println it
		}*/
	}
	
	def getEndTags( String text ){
		//	List<String> tags = text.findAll("</?[A-Za-z][A-Za-z0-9]*(\\s+[a-zA-Z0-9]+=(\'|\")?\\w*-?:?%?(\'|\")?)*\\s*/?>" )
			List<String> tags  = text.findAll("</(.+?)/?>")
			/*tags.each {
				// println it
			}*/
		}
	
	def getStartTags(List<String> allTags , List<String> endTags){
		allTags-endTags
	}
	
	def findUnclosedStartTags(List<String> startTags , List<String> endTags){
		def badTags = []
		startTags.each { startTag ->
			String modifiedStart = ""
			if(startTag.contains(" ")){
				int indexInt = startTag.indexOf(" ")
				modifiedStart = startTag.substring(1,indexInt) + ">"
			}
			else {
				modifiedStart =  startTag.substring(1)
			}
			if(endTags.contains("</"+modifiedStart)) return;
				// println "good Tag " + startTag
			else if(startTag.endsWith("/>")) return ;
				// println "Short good tag " + startTag
//			else if(startTag.startsWith("<BR>"));
//				// println "Ignore Break"
			else
				badTags << startTag
		}
		badTags
	}
	
	def findUnopenedEndTags( List<String> startTags , List<String> endTags ){
		def badTags = []
		endTags.each { endTag ->
			String modifiedEnd =  endTag.substring(2,endTag.length()-1)
			
			if(startTags.contains("<"+modifiedEnd +">")){
				// println "good starts Tag " + endTag
			}else if(startTags*.startsWith("<"+modifiedEnd +" ").contains(true) ){
				// println "good Tag " + endTag
			}else{
				badTags << endTag
			}
		}
		badTags
	}
	
	def fixStartTag( String text, List<String> tags ){
		tags.each{ tag ->
			text = text.replaceAll( tag, tag.substring(0, tag.length() - 1 ) + "/>" )
		}

		text
	}
	
	def fixEndTag( String text, List<String>  tags ){
		tags.each{ tag ->
			text = text.replaceAll( tag, " " )
		}

		text
	}
	
	def getTagCountMap( List<String> tagList, int ignoreIndex ){
		def tagMap = [:]
		tagList.each {
			String modifiedStart = ""
			if( it.contains( " " ) ){
//				int indexInt = it.indexOf( " " )
//				modifiedStart = it.substring( ignoreIndex, indexInt )
				if( it.endsWith("/>")){
					return
				} else {
					int indexInt = it.indexOf( " " )
					modifiedStart = it.substring( ignoreIndex, indexInt )
				}
			} else {
				modifiedStart =  it.substring( ignoreIndex, it.length() - 1 )
			}
			if( tagMap.containsKey(modifiedStart)){
				tagMap[modifiedStart]++
			} else {
				tagMap.put( modifiedStart, 1 )
			}
		}
		tagMap
	}
	
	def findRepeatingIncompleteTags(def text, def startMap, def endMap, List<String> startTags, List<String> endTags ){
		def startKeys = startMap*.key
		def endKeys = endMap*.key
		//def changedKeys = startKeys.findAll { startMap[ it ] != endMap[ it ] }
		def missingStartKeys = startKeys.findAll { startMap[ it ] < endMap[ it ] }
		text = fixRepeatingEndKeys(text, missingStartKeys)
		
		
		def missingEndKeys = startKeys.findAll { startMap[ it ] > endMap[ it ] }
	//	text = fixRepeatingStartKeys(text, missingEndKeys)
		
		text
		//changedKeys
	}
	
	def fixRepeatingStartKeys(String text, List<String> tags){
		tags.each{ tag ->
			String startTag = "<"+tag;
			List<Integer> startIndices = findAllIndices( text, startTag, [] )
			String endTag = "</"+tag
			List<Integer> endIndices = findAllIndices(text, endTag, [] )
			for( int i = 0; i < startIndices.size(); i++){
				if(i+1 < startIndices?.size() && i < endIndices?.size() ){
					if(startIndices.get(i+1) < endIndices[i]){
						//fix this one
						text = removeUnwantedStartTags(text, startIndices[i], tag)
						break;
					}
				} else {
					// fix this one
					text = removeUnwantedStartTags(text, startIndices[i], tag)
					break;
				}
			}
		
		}

		text
	}
	
	def fixRepeatingEndKeys(String text, List<String> tags){
		tags.each{ tag ->
			String startTag = "<"+tag;
			List<Integer> startIndices = findAllIndices( text, startTag, [] )
			String endTag = "</"+tag
			List<Integer> endIndices = findAllIndices(text, endTag, [] )
			for( int i = 0; i < endIndices.size(); i++){
				if(startIndices?.size() ){
					if(i<startIndices.size()){
						for(int j = i ; j<startIndices.size(); j++)
							if(i!=0){
								if(!(endIndices[i] > startIndices[j] && startIndices[j]> endIndices[i-1])){
									//fix this one
									text = removeUnwantedEndTags(text, endIndices[i], tag)
									break;
								}
							}else{
								if(endIndices[i] < startIndices[j]){
									//fix this one
									text = removeUnwantedEndTags(text, endIndices[i], tag)
									break;
								}
							}
					}else{
						text = removeUnwantedEndTags(text, endIndices[i], tag)
						continue;
					}
				} else {
					// fix this one
					text = removeUnwantedEndTags(text, endIndices[i], tag)
					continue;
				}
			}
		
		}

		text
	}
	

	private String removeUnwantedStartTags(String text, int i, String tag) {
		String text1 = text.substring(0,i)
		String text2 = text.substring(i + tag.length()+2)
		// println "text1: ${text1}"
		// println "text2: ${text2}"
		//text.substring(startIndices[i], startIndices[i] + tag.length()) = ""
		text = text1+text2
		return text
	}
	
	private String removeUnwantedEndTags(String text, int i, String tag) {
		String text1 = text.substring(0,i)
		String text2 = text.substring(i + tag.length()+3)
		// println "text1: ${text1}"
		// println "text2: ${text2}"
		//text.substring(startIndices[i], startIndices[i] + tag.length()) = ""
		text = text1+text2
		return text
	}
	
	def findAllIndices( String text, String tag, def indices ){
		//while( text ) {
			int newIndex = text.indexOf( tag )
			if( newIndex != -1 ){
				if(indices.size()>0){
					indices << (indices.getAt(indices.size()-1) + tag.length() + newIndex)
				} else {
					indices << newIndex
				}
				text = text.substring( newIndex + tag.length() )
				if( text.indexOf( tag ) != -1 ){
					//indices << findAllIndices( text, tag, indices )
					findAllIndices( text, tag, indices )
				}
			} else {
				return
			}
		//}
		indices
	}
	
	/*def removeUnopenedHTMLTags(String originalString, def tagsToCheck) {
		def firstClose = 0
		def firstOpen = 0
		def retString = ""
		
		tagsToCheck.each() {
			def openTag = "<${it}"
			def closeTag = "</${it}>"
			firstClose = originalString.indexOf(closeTag)
			firstOpen = originalString.indexOf(openTag)
			while ( (firstClose < firstOpen) && (firstOpen != -1) || ( (firstClose > firstOpen) && firstOpen == -1) ) {
				def part1 = originalString.substring(0, firstClose + closeTag.length())
				def part2 = originalString.substring(firstClose + closeTag.length(), originalString.length())
				part1 = part1.replace(closeTag, "")
				retString = part1 + part2
				originalString = retString
				firstClose = originalString.indexOf(closeTag)
				firstOpen = originalString.indexOf(openTag)
			}
		}
		originalString
	}*/
	
	def fixQuestions(List<String> questions){
		List<String> modifiedQs = []
		questions.each { text ->
			text = getHtmlTagReplacer(text, false)
			List<String> allTags = getAllTags(text)
			
			List<String> endTags = getEndTags(text)
			List<String> startTags = getStartTags(allTags,endTags)
			
			def lonelyStartTags = findUnclosedStartTags(startTags, endTags)
			text = fixStartTag( text, lonelyStartTags )
			
			def lonelyEndTags = findUnopenedEndTags(startTags, endTags)
			text = fixEndTag( text, lonelyEndTags )
			
		//	def startMap = getTagCountMap( startTags, 1 )
		//	def endMap = getTagCountMap( endTags, 2 )
		//	text = findRepeatingLonelyTags( text, startMap, endMap, startTags, endTags )
			
			modifiedQs << text
		}
		
		modifiedQs
	}
	
	
	def fixQuestion(String text){
		//	text = getHtmlTagReplacer(text, false)
			List<String> allTags = getAllTags(text)
			
			List<String> endTags = getEndTags(text)
			List<String> startTags = getStartTags(allTags,endTags)
			
			def lonelyStartTags = findUnclosedStartTags(startTags, endTags)
			text = fixStartTag( text, lonelyStartTags )
			
			def lonelyEndTags = findUnopenedEndTags(startTags, endTags)
			text = fixEndTag( text, lonelyEndTags )
			
		//	def startMap = getTagCountMap( startTags, 1 )
		//	def endMap = getTagCountMap( endTags, 2 )
		//	text = findRepeatingLonelyTags( text, startMap, endMap, startTags, endTags )
			
			text
	}
	
	/*def getHtmlTagReplacer(def text, def isHtmlToText) {
		String regex = "<body>.*</body>";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		while (m.find()) {
			String replacement = m.group();
			
			if(isHtmlToText) {
				replacement = replacement.substring(replacement.indexOf("<body>") + "<body>".length(), replacement.indexOf("</body>"));
				replacement = replacement.replaceAll("<", "&lt;" );
				replacement = replacement.replaceAll(">", "&gt;");
				m.appendReplacement(sb, "<body>$replacement</body>");
			}
			else {
				replacement = replacement.replaceAll("&lt;", "<");
				replacement = replacement.replaceAll("&gt;", ">");
				m.appendReplacement(sb, replacement);
			}
		}
		m.appendTail(sb);
		sb.toString()
	}*/
	
	def getHtmlTagReplacer(String text, def isHtmlToText) {
		String regex = "<body>(.*)</body>";
		//text = text.replaceAll("<body>\\n", "<body>")
		//text = text.replaceAll("\\n</body>", "</body>")
		text = text.replaceAll("\\n", " ")
		text = text.replaceAll("<body>", "\n<body>")
	
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		while (m.find()) {
			String replacement = m.group();
			
			if(isHtmlToText) {
				replacement = replacement.substring(replacement.indexOf("<body>") + "<body>".length(), replacement.indexOf("</body>"));
				replacement = replacement.replaceAll("<", "&lt;" );
				replacement = replacement.replaceAll(">", "&gt;");
				m.appendReplacement(sb, "<body>$replacement</body>");
			}
			else {
				replacement = replacement.substring(replacement.indexOf("<body>") + "<body>".length(), replacement.indexOf("</body>"));
				replacement = replacement.replaceAll("&lt;", "<");
				replacement = replacement.replaceAll("&gt;", ">");
				m.appendReplacement(sb, "<body>$replacement</body>");
			}
		}
		m.appendTail(sb);
		sb.toString()
	}
	
	static main(args){
		GpHtmlParser html = new GpHtmlParser()
		String text = """   <question questionid="al1cmine_02" entityid="2785530" version="1.2" resourceentityid="2785530,775,55E" creationdate="2013-08-25T03:02:52.267Z" creationby="1664041" modifieddate="2013-08-25T03:14:33.13Z" modifiedby="1664041" flags="4" actualentityid="2785530" schema="2" partial="false">
    <answer>
      <value>al1cmine_02_2</value>
    </answer>
    <learningobjectives>
      <objective id="14201ca7-f948-667e-7020-dfa3d4dfcb7a" />
    </learningobjectives>
    <body>&lt;b&gt;Solve the inequality.&lt;/b&gt;&lt;br&gt;&lt;br&gt;&lt;img width="113" height="15" align="bottom" alt="mc002-1.jpg" src="[~]/testitemimages/algebra_1a/more_equations_and_inequalities/al1cmine-mc002-1.jpg" border="0"&gt;</body>
    <interaction type="choice">
      <choice id="al1cmine_02_1">
        <body>&lt;img width="65" height="15" align="bottom" alt="mc002-2.jpg" src="[~]/testitemimages/algebra_1a/more_equations_and_inequalities/al1cmine-mc002-2.jpg" border="0"&gt;</body>
      </choice>
      <choice id="al1cmine_02_3">
        <body>&lt;img width="65" height="15" align="bottom" alt="mc002-4.jpg" src="[~]/testitemimages/algebra_1a/more_equations_and_inequalities/al1cmine-mc002-4.jpg" border="0"&gt;</body>
      </choice>
      <choice id="al1cmine_02_4">
        <body>&lt;img width="72" height="15" align="bottom" alt="mc002-5.jpg" src="[~]/testitemimages/algebra_1a/more_equations_and_inequalities/al1cmine-mc002-5.jpg" border="0"&gt;</body>
      </choice>
      <choice id="al1cmine_02_2">
        <body>&lt;img width="65" height="15" align="bottom" alt="mc002-3.jpg" src="[~]/testitemimages/algebra_1a/more_equations_and_inequalities/al1cmine-mc002-3.jpg" border="0"&gt;</body>
      </choice>
    </interaction>
  </question>
"""
		

		println "started"
		println "->" + text
		text = html.getHtmlTagReplacer(text, false)
		println "->" + text
		/*Source source = new Source(text);
		text = source.getSourceFormatter().setIndentString("")
			.setTidyTags(true).setCollapseWhiteSpace(true)
			.setIndentAllElements(true).toString();
			println "->" + text
			*/
		println html.fixQuestion(text)
		text = html.getHtmlTagReplacer(text, true)
		
		println text
		text ="<requests>$text</requests>"
		
	//	DlapApi dlap = new DlapApi( Constants.dlapHost )
	//	dlap.login("komal1234", "admin", "password")
		
		
	//	def success =  dlap.putQuestions(text)
		
		//text = html.fixBrTags( text )
	//	// println text
		//List<String> allTags = html.getAllTags(text)
		
		//List<String> endTags = html.getEndTags(text)
		//List<String> startTags = html.getStartTags(allTags,endTags)
		
		/*// // println startTags
		// println endTags
		
		def lonelyStartTags = html.findLonelyStartTags(startTags, endTags)
		text = html.fixStartTag( text, lonelyStartTags )
		// println text
		
		def lonelyEndTags = html.findLonelyEndTags(startTags, endTags)
		// println lonelyEndTags
		text = html.fixEndTag( text, lonelyEndTags )
		// println text*/
		
		//def startMap = html.getTagCountMap( startTags, 1 )
	//	def endMap = html.getTagCountMap( endTags, 2 )
		// println html.findRepeatingIncompleteTags( text, startMap, endMap, startTags, endTags )
	}
}
