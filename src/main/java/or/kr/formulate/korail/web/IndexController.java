package or.kr.formulate.korail.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.scan.JarFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @GetMapping(path = {"","/"})
    public String index() {
        return "Hello World";
    }

    @GetMapping(path = "output")
    public Map<String, Object> output() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        return map;
    }

    @PostMapping(path = "input")
    public Map<String, Object> input(@RequestBody Map<String, Object> input) {
        logger.debug("{}", input);
        return input;
    }

    @PostMapping(path = "input2")
    public String input2(@RequestBody Map<String, Object> input) throws JsonProcessingException {
        logger.debug("{}", input);
        ObjectMapper objectMapper = new ObjectMapper();


        List<String> list = new ArrayList<>();
        list.add("One");
        list.add("Two");
        list.add("Three");


        input.put("adder", "addValue");
        input.put("list", list);
        return objectMapper.writeValueAsString(input);

    }


}
