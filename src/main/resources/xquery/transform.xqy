xquery version "3.0";

import module namespace transform = "transform" at "transform.xqm";


declare variable $namespaces as map(xs:string, xs:string) external;
declare variable $structure as xs:string external;
declare variable $mapping as xs:string external;
declare variable $input-dir as xs:string external;
declare variable $output-dir as xs:string external;



declare function local:pad-with-zeroes(
        $number as xs:string,
        $length as xs:integer
) as xs:string {
  if (fn:string-length($number) = $length) then $number
  else local:pad-with-zeroes(fn:concat("0", $number), $length)
};

for $source-path-relative in file:list($input-dir, fn:false(), "*.xml,*.XML")
let $source-document := fn:doc(fn:concat($input-dir, file:dir-separator(), $source-path-relative))
for $target-document at $count in transform:transform($source-document, $mapping, $namespaces, $structure)
let $target-path := fn:concat(
        $output-dir,
        file:dir-separator(),
        $target-document/*:ead/*:eadheader/*:eadid/text(),
        ".", fn:substring-after($source-path-relative, ".")
)

let $p := if(file:exists($target-path)) then fn:concat(fn:substring-before($target-path, ".xml"), "_", local:pad-with-zeroes(fn:string($count), 9), "_DUPLICATE.xml") else $target-path


return file:write($p, $target-document, map { "omit-xml-declaration": "no" })
