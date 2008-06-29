import com.canoo.webtest.WebtestCase
import org.junit.Test

/**
The same tests as in googleJamwikiTest.xml and googleWebTestSteps.xml but expressed as Groovy code
*/
public class GoogleJamwikiTest extends WebtestCase
{
	private static xpathSearchResults = "//a[@class='l']"

	
	void testGoogle()
	{
		webtest("check that jamwiki.org is Google's top 'jamwiki' result [test written as Groovy code]")
		{
			invoke url: "http://www.google.com/ncr", description: "Go to Google (in English)"
			verifyTitle "Google"
			setInputField name: "q", value: "jamwiki"
			clickButton "I'm Feeling Lucky"
			verifyTitle "StartingPoints - JAMWiki"
		}
	}


	void testSearchForGoogleSteps()
	{
		webtest("Search some jamwiki pages using Google restricted to jamwiki.org [test written as Groovy code]")
		{
			googleOnWebTest "Features",
			{
				verifyXPath xpath: xpathSearchResults, text:"Features - JAMWiki", 
					description: "Verify that clickLink's documentation is the first result"
			}
			googleOnWebTest "Configuration",
			{
				verifyXPath xpath: xpathSearchResults, text:"Configuration - JAMWiki", 
					description: "Verify that setFileField's documentation is the first result"
			}
			googleOnWebTest "notExistingStep",
			{
				verifyText text: "Your search .* did not match any documents.", regex: "true"
			}
		}
	}

	/**
	 * Extracted for reuse
	 * @param searchText the text to search for
	 * @param verification the steps to executed once the results page is reached
	 */
	private googleOnWebTest(String searchText, Closure verification)
	{
		ant.group description: "search for $searchText",
		{
			invoke url: "http://www.google.com/ncr", description: "Go to Google (in English)"
			clickLink "Advanced Search"
			setInputField description: "Set the search value", name: "as_q", value: searchText
			setInputField description: "Restrict search to jamwiki's website", name: "as_sitesearch", value: "jamwiki.org"
			clickButton label: "Advanced Search"
			verification.delegate = ant
			verification()
		}
	}
}
