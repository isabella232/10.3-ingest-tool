xquery version "3.0";

import module namespace ead2html = "ead2html" at "ead2html.xqm";

declare variable $input-dir as xs:string external;
declare variable $output-dir as xs:string external;
declare variable $stylesheet-location as xs:string external;
declare variable $formatting as xs:string external;
declare variable $translations as xs:string external;
declare variable $language as xs:string external;

(: serialization options :)
let $csv_options := map { "separator": "tab", "header": "yes" }
let $html_options := map { "method": "html", "media-type": "text/html", "include-content-type": "yes" }

(: read configurations :)
let $formatting-configuration := csv:parse($formatting, $csv_options)
let $translation-configuration := csv:parse($translations, $csv_options)

(: transform each injected XML document to HTML :)
let $index-items := for $document-name in file:list($input-dir, fn:false(), "*.xml,*.XML")
  let $document := fn:doc(fn:concat($input-dir, file:dir-separator(), $document-name))
  let $document-name := fn:replace($document-name, "\.(xml|XML)$", "")
  let $html := ead2html:document-to-html($document-name, $document, $stylesheet-location, $formatting-configuration, $translation-configuration, $language)
  
  (: write the HTML to file :)
  let $html-name := fn:concat($document-name, ".html")
  let $html-path := fn:concat($output-dir, file:dir-separator(), $html-name)
  let $void := file:write($html-path, $html, $html_options)
  
  (: return a link to the HTML file and the number of errors in it :)
  let $num-errors := fn:count($html/html/body/div[@class = "table-of-contents"]/a)
  return <div class="index-item { if ($num-errors > 0) then "with-errors" else "without-errors" }">
      <a href="{ $html-name }">{ $document-name }</a>
      <span class="num-errors">{ $num-errors }</span>
    </div>

(: construct the index :)
let $index := document {
  <html>
    <head>
      <link rel="stylesheet" href="{ $stylesheet-location }" />
      <title>EAD Preview Index</title>
    </head>
    <body>
      <div class="index">
        { $index-items }
      </div>
    </body>
  </html>
}

(: write the index to file :)
let $index-path := fn:concat($output-dir, file:dir-separator(), "index.html")
return file:write($index-path, $index, $html_options)
