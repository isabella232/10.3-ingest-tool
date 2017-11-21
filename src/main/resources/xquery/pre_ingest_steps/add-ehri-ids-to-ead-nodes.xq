xquery version "3.0";

import module namespace functx="http://www.functx.com" at "functx-1.0-nodoc-2007-01.xq";

declare variable $input as xs:string external;
declare variable $top as xs:boolean := fn:false();
declare variable $hasHead as xs:boolean := fn:exists(doc($input)//head);
declare variable $changeNode as item() :=
    <change>
        <date>{fn:format-dateTime(fn:current-dateTime(),"[Y0001]/[M01]/[D01] [H01]:[m01]:[s01]")}</date>
        <item>EHRI added a unitid with label "ehri_internal_identifier" to give every node a unique id.</item>
    </change>;

declare variable $count := 0;

declare function local:appendChild($node as item(), $chld as item()*){
    let $insert := %updating function($node, $child) { insert node $child into $node }
    return $node update (updating $insert(., $chld))
};

declare function local:traverseXML($node as item()) as item()*{
    for $child at $x in $node/element()
    return (
        let $new  := element {fn:node-name($child)}{$child/text()}
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "revisiondesc")                                        then (local:appendChild($new, $changeNode)) else $new
        let $top  := if (fn:local-name-from-QName(fn:node-name($child)) = "archdesc")                                            then fn:true() else $top
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "did" and $top = fn:true() and $hasHead = fn:false())  then (local:appendChild($temp, <unitid label="ehri_internal_id">0</unitid>)) else $temp
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "did" and $top = fn:false())                           then (local:appendChild($temp, <unitid label="ehri_internal_id">{$x}</unitid>)) else $temp
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "eadheader" and fn:not(fn:exists($child/@langusage)))  then functx:add-or-update-attributes($temp, fn:QName("","langusage"), "") else $temp
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "did" and fn:not(fn:exists($child/unittitle)))         then (local:appendChild($temp, <unittitle>{substring($child/scopecontent/text(), 1, 20)}</unittitle>)) else $temp
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "did")                                                 then (local:appendChild($temp, <unitid label="ehri_main_identifier">{$child/unitid[1]/text()}</unitid>)) else $temp
        let $temp := local:appendChild($temp, local:traverseXML($child))
        let $top  := if (fn:local-name-from-QName(fn:node-name($child)) = "did")                                                 then fn:false() else $top
        let $temp := if (fn:local-name-from-QName(fn:node-name($child)) = "head" and $top = fn:true())                           then (local:appendChild($temp, <unitid label="ehri_internal_id">0</unitid>)) else $temp
        return (
            functx:copy-attributes($temp, $child)
        )
    )
};


let $node := doc($input)/node()
return (
    local:appendChild(element{fn:node-name($node)}{}, local:traverseXML($node))
)


