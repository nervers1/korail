package or.kr.formulate.korail.web;

import or.kr.formulate.korail.util.CommonUtil;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MarkdownController {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownController.class);
    private final static String LOCAL_MANUAL_PATH = "static/manuals/";


    @GetMapping("/view/{page}")
    public String markdownView(@PathVariable("page") String page, Model model) throws Exception {

        String markdownValueFormLocal = CommonUtil.getMarkdownValueFormLocal(page);

        logger.info("---> {}", markdownValueFormLocal);
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownValueFormLocal);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        model.addAttribute("contents", renderer.render(document));

        return "view";
    }

}
