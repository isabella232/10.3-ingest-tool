xquery version "3.0";

declare variable $input-dir as xs:string external;

declare function local:parse-json($json as xs:string) as item() {
  json:parse($json, map { "format": "attributes" })
};

declare function local:parse-csv($csv as xs:string) as item() {
  csv:parse($csv, map { "format": "attributes", "separator": "comma", "header": "yes" })
};

declare function local:parse-tsv($tsv as xs:string) as item() {
  csv:parse($tsv, map { "format": "attributes", "separator": "tab", "header": "yes" })
};

let $encoding := "UTF-8"
return for $source-path-relative in file:list($input-dir, fn:false())
  let $source-path := fn:concat($input-dir, file:dir-separator(), $source-path-relative)
  let $source := file:read-text($source-path, $encoding)
  let $target-path := fn:concat($input-dir, file:dir-separator(), fn:replace($source-path-relative, "\.[^\.]*$", ".xml"))
  let $target :=
    if (fn:matches($source-path-relative, "\.(json|JSON)$")) then local:parse-json($source)
    else if (fn:matches($source-path-relative, "\.(csv|CSV)$")) then local:parse-csv($source)
    else if (fn:matches($source-path-relative, "\.(tsv|TSV)$")) then local:parse-tsv($source)
    else ()
  
  return if ($target) then file:write($target-path, $target, map { "omit-xml-declaration": "no" }) else ()
