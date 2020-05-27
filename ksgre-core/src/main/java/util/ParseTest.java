package main.java.util;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import main.java.dataType.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ParseTest {
    private static Logger log = Logger.getLogger(ParseTest.class);

    private static EnhancedTestCase tc;
    public static String folder;

    private static String baseUrl;

    public ParseTest(String f) {
        super();
        folder = f;
        baseUrl = "";
    }

    public static class PackageVisitor extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(PackageDeclaration p, Object arg) {
            p.setName(new NameExpr(arg.toString()));
        }
    }

    public static class ChangeMethodVisitor extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (n.getAnnotations() != null && n.getAnnotations().get(0).getName().getName().equals("Test") && n.getName().equals(tc.getName())) {
                BlockStmt newBlockStmt = new BlockStmt();

                for (Integer i : tc.getStatements().keySet()) {
                    main.java.dataType.Statement statement = tc.getStatements().get(i);
                    statement.toString();
                    ASTHelper.addStmt(newBlockStmt, new NameExpr(tc.getStatements().get(i).toString()));
                }

                n.setBody(newBlockStmt);
            }
        }
    }

    public static void parseAndSaveToJava(EnhancedTestCase newTest, String oldPath, String newPath) throws IOException {
        tc = newTest;
        CompilationUnit cu = null;

        try {
            cu = JavaParser.parse(new File(oldPath));
        } catch (ParseException | IOException e) {
            log.error("an error occurred when parsing a java file");
            e.printStackTrace();
        }

        // 获取并修改包名
        String packageName = UtilsParser.getPackageName(newPath);
        changePackage(cu, packageName);

        // 修改语句
        new ChangeMethodVisitor().visit(cu, oldPath);

        // 持久化新测试用例到文件
        String source = cu.toString();
        File file = new File(newPath);
        FileUtils.touch(file);
//        FileUtils.writeStringToFile(file, source);
        FileUtils.write(file, source, "utf-8");
    }

    /**
     * 修改包的声明
     * @param cu
     * @param packageName
     */
    private static void changePackage(CompilationUnit cu, String packageName) {
        new PackageVisitor().visit(cu, packageName);
    }

    public EnhancedTestCase parseAndSerialize(String clazz) {
        CompilationUnit cu = null;

        try {
            cu = JavaParser.parse(new File(clazz));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        new MethodVisitor().visit(cu, clazz);
        UtilsParser.serializeTestCase(tc, clazz, folder);

        return tc;
    }

    // Simple visitor implementation for visiting MethodDeclaration nodes.
    private static class MethodVisitor extends VoidVisitorAdapter<Object> {
        @Override
        public void visit(MethodDeclaration m, Object arg) {
            // 提取baseUrl
            if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Before")) {
                for (Statement st: m.getBody().getStmts()) {
                    if (st.toString().contains("baseUrl")) {
                        String tempStr = st.toString();
                        baseUrl = StringUtils.substringsBetween(tempStr, "\"", "\"")[0];
                    }
                }
            }
            // 提取核心测试内容
            else if (m.getAnnotations() != null && m.getAnnotations().get(0).getName().getName().equals("Test")) {

                String className = "";
                try {
                    className = UtilsParser.getClassNameFromPath((String) arg);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                String fullPath = arg.toString();
                tc = new EnhancedTestCase(m.getName(), fullPath);
                for (Statement st : m.getBody().getStmts()) {
                    /*
                     * Driver get is managed separately, but current implementation does not support
                     * it fully. The driver.get commands are expected to be moved into the setUp
                     * method of the test class.
                     */
                    if (st.toString().contains("driver.get(")) {
                        DriverGet dg = new DriverGet();
                        dg.setAction("get");
                        dg.setLine(st.getBeginLine());

                        try {
                            dg.setValue(baseUrl + UtilsParser.getUrlFromDriverGet(st.toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tc.addStatementAtPosition(dg.getLine(), dg);
                    }
                    else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
                            && !st.toString().contains("new Select")) {
                        EnhancedWebElement ewe = new EnhancedWebElement();
                        int line = st.getBeginLine();
                        ewe.setLine(line);
                        try {
                            ewe.setDomLocator(UtilsParser.getDomLocator(st.toString()));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        if (st.toString().contains("click()")) {
                            ewe.setAction("click");
                            ewe.setValue("");
                        } else if (st.toString().contains("sendKeys")) {
                            ewe.setAction("sendKeys");
                            try {
                                ewe.setValue(UtilsParser.getValueFromSendKeys(st.toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (st.toString().contains("getText")) {
                            ewe.setAction("getText");
                            ewe.setValue("");
                        } else if (st.toString().contains("clear")) {
                            ewe.setAction("clear");
                            ewe.setValue("");
                        }

                        try {
                            DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line,
                                    "domInfo", getFolder());
                            ewe.setTagName(info.getTagName());
                            ewe.setXpath(info.getXPath());
                            ewe.setId(info.getId());
                            ewe.setClassAttribute(info.getClassAttribute());
                            ewe.setNameAttribute(info.getNameAttribute());
                            ewe.setText(info.getTextualContent());
                            ewe.setValueAttribute(info.getValueAttribute());
                            ewe.setTypeAttribute(info.getTypeAttribute());
                            ewe.setTitleAttribute(info.getTitleAttribute());
                            ewe.setAppend(info.getAppend());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tc.addStatementAtPosition(line, ewe);
                    }
                    // select
                    else if (st.toString().contains("driver.findElement(") && !st.toString().contains("assert")
                            && st.toString().contains("new Select")) {

                        EnhancedSelect esl = new EnhancedSelect();
                        int line = st.getBeginLine();
                        esl.setLine(line);
                        try {
                            esl.setDomLocator(UtilsParser.getDomLocator(st.toString()));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        if (st.toString().contains("selectByVisibleText")) {
                            esl.setAction("selectByVisibleText");
                            esl.setValue(UtilsParser.getValueFromSelect(st));
                        } else if (st.toString().contains("selectByIndex")) {
                            esl.setAction("selectByIndex");
                            esl.setValue(UtilsParser.getValueFromSelect(st));
                        } else if (st.toString().contains("selectByValue")) {
                            esl.setAction("selectByValue");
                            esl.setValue(UtilsParser.getValueFromSelect(st));
                        }

                        try {
                            /* get the other DOM information. */
                            DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line,
                                    "domInfo", getFolder());
                            esl.setTagName(info.getTagName());
                            esl.setXpath(info.getXPath());
                            esl.setId(info.getId());
                            esl.setClassAttribute(info.getClassAttribute());
                            esl.setNameAttribute(info.getNameAttribute());
                            esl.setText(info.getTextualContent());
                            esl.setValueAttribute(info.getValueAttribute());
                            esl.setTypeAttribute(info.getTypeAttribute());
                            esl.setTitleAttribute(info.getTitleAttribute());
                            esl.setAppend(info.getAppend());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tc.addStatementAtPosition(line, esl);
                    }
                    // assertion
                    else if (st.toString().contains("driver.findElement(") && st.toString().contains("assert")) {

                        EnhancedAssertion ea = new EnhancedAssertion();
                        int line = st.getBeginLine();
                        ea.setAssertion(UtilsParser.getAssertion(st));
                        ea.setPredicate(UtilsParser.getPredicate(st));

                        if (st.toString().contains("getText")) {
                            ea.setAction("getText");
                            ea.setValue(UtilsParser.getValueFromAssertion(st));
                        } else {
                            log.error("Analysing an assertion with no getText()");
                        }

                        ea.setLine(line);
                        try {
                            ea.setDomLocator(UtilsParser.getDomLocator(st.toString()));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        try {
                            /* get the other DOM information. */
                            DOMInformation info = UtilsFileGetters.getDOMInformationFromJsonFile(className, line,
                                    "domInfo", getFolder());
                            ea.setTagName(info.getTagName());
                            ea.setXpath(info.getXPath());
                            ea.setId(info.getId());
                            ea.setClassAttribute(info.getClassAttribute());
                            ea.setNameAttribute(info.getNameAttribute());
                            ea.setText(info.getTextualContent());
                            ea.setValueAttribute(info.getValueAttribute());
                            ea.setTypeAttribute(info.getTypeAttribute());
                            ea.setTitleAttribute(info.getTitleAttribute());
                            ea.setAppend(info.getAppend());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tc.addStatementAtPosition(line, ea);

                    }
                    // assertAlert
                    else if (st.toString().contains("assert") && st.toString().contains("closeAlertAndGetItsText")) {
                        EnhancedAssertion ea = new EnhancedAssertion();
                        int line = st.getBeginLine();
                        ea.setAssertion(UtilsParser.getAssertion(st));
                        ea.setPredicate(UtilsParser.getPredicate(st));

                        if (st.toString().contains("closeAlertAndGetItsText")) {
                            ea.setAction("closeAlertAndGetItsText");
                            ea.setValue(UtilsParser.getValueFromAssertion(st));
                        } else {
                            log.error("Analysing an assertion with no getText()");
                        }

                        ea.setLine(line);
                        tc.addStatementAtPosition(line, ea);
                    }
                    // alert
                    else if (st.toString().contains("alert().accept()")) {
                        EnhancedAlert ea = new EnhancedAlert();
                        int line = st.getBeginLine();
                        ea.setLine(line);
                        ea.setAction("alert");
                        tc.addStatementAtPosition(line, ea);
                    }
                }
            }
        }
    }

    public static String getFolder() {
        return folder;
    }

    public static void setFolder(String folder) {
        ParseTest.folder = folder;
    }
}
