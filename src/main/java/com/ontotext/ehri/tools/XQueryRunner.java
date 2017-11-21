package com.ontotext.ehri.tools;

import org.basex.core.Context;
import org.basex.core.StaticOptions;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.util.UriResolver;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.Str;
import org.basex.query.value.type.AtomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XQueryRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryRunner.class);
    private static final Context CONTEXT = new Context();
    private static final UriResolver URI_RESOLVER = (path, uri, base) -> {
//        String fullPath = Configuration.getString("xquery-module-dir") + File.separator + path;
        String fullPath = Configuration.getString("xquery-module-dir") + "/" + path;
        //return new IOContent(TextReader.readText(fullPath));
        return new IOFile(TextReader.resolvePath(fullPath));
    };

    public static void customTransform(File xqueryFile, File inputDir, File outputDir) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("input-dir", inputDir.getAbsolutePath());
        variables.put("output-dir", outputDir.getAbsolutePath());
        run(xqueryFile.getAbsolutePath(), variables);
    }

    public static void genericTransform(String mapping, File inputDir, File outputDir) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("namespaces", Configuration.get("ead-namespaces"));
        variables.put("structure", TextReader.readText(Configuration.getString("ead-struct-path")));
        variables.put("mapping", mapping);
        variables.put("input-dir", inputDir.getAbsolutePath());
        variables.put("output-dir", outputDir.getAbsolutePath());
        run(Configuration.getString("generic-transformer-path"), variables);
    }

    public static void generateHTML(File injectedDir, File htmlDir, String language) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("input-dir", injectedDir.getAbsolutePath());
        variables.put("output-dir", htmlDir.getAbsolutePath());
        variables.put("stylesheet-location", TextReader.resolvePath(Configuration.getString("stylesheet-location")).getAbsolutePath());
        variables.put("formatting", TextReader.readText(Configuration.getString("formatting-path")));
        variables.put("translations", TextReader.readText(Configuration.getString("translations-path")));
        variables.put("language", language);
        run(Configuration.getString("html-generator-path"), variables);
    }

    public static void convertToXML(File inputDir) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("input-dir", inputDir.getAbsolutePath());
        run(Configuration.getString("xml-converter-path"), variables);
    }

    public static void run(String xqueryPath, Map<String, Object> variables) {
        String xquery = TextReader.readText(xqueryPath);
        QueryProcessor processor = new QueryProcessor(xquery, CONTEXT).uriResolver(URI_RESOLVER);
        bindVariables(processor, variables);

        try {
            processor.value();
        } catch (QueryException e) {
            LOGGER.error("failed to execute XQuery: " + xqueryPath, e);
        }
    }

    private static void bindVariables(QueryProcessor processor, Map<String, Object> variables) {

        for (String name : variables.keySet()) {
            Object value = variables.get(name);

            try {
                if (value instanceof String) processor.bind(name, value, "xs:string");
                else if (value instanceof Map) processor.bind(name, basexMap(value), "map()");
                else LOGGER.warn("unsupported value type: " + value.getClass().getName());
            } catch (QueryException e) {
                LOGGER.error("failed to bind variable \"" + name + "\" to value: " + value.toString(), e);
            }
        }
    }

    private static org.basex.query.value.map.Map basexMap(Object javaMap) {
        org.basex.query.value.map.Map basexMap = org.basex.query.value.map.Map.EMPTY;

        if (!(javaMap instanceof Map)) {
            LOGGER.error("object is not an instance of " + Map.class.getName() + ": " + javaMap.toString());
            return basexMap;
        }

        Map map = (Map) javaMap;
        for (Object key : map.keySet()) {

            try {
                basexMap = basexMap.put(basexItem(key), basexItem(map.get(key)), null);
            } catch (QueryException e) {
                LOGGER.error("failed to put key into map: " + key.toString());
            }
        }

        return basexMap;
    }

    private static Item basexItem(Object javaObject) {
        if (javaObject instanceof String) return new Str(((String) javaObject).getBytes(Configuration.ENCODING), AtomType.STR);

        LOGGER.error("unsupported item type: " + javaObject.getClass().getName());
        return null;
    }
}
