xquery version "3.0";

module namespace foo="http://exist-db.org/apps/ns/foo";

declare function foo:process-node($node as node()?, $model) {
    if ($node) then
    typeswitch($node)
        case text() return $node
        case element(messageHeader) return foo:messageHeader($node, $model)
        default return element { $node/name() }
                               { $node/@*, foo:recurse($node, $model) }

    else ()
};

declare function foo:recurse($node as node()?, $model) as item()* {
    if ($node)
    then
        for $cnode in $node/node()
        return foo:process-node($cnode, $model)
    else ()
};

declare function foo:messageHeader($node as node(), $model) {
element { $node/name() }
        { $node/@*,
          attribute { 'newAtt' } { 'newVal' },
          foo:recurse($node, $model)
        }
};