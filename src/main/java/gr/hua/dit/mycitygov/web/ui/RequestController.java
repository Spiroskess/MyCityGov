package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.CreateRequestRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final RequestTypeRepository requestTypeRepository;
    private final AuthUtils authUtils;

    public RequestController(RequestService requestService,
                             RequestTypeRepository requestTypeRepository,
                             AuthUtils authUtils) {
        this.requestService = requestService;
        this.requestTypeRepository = requestTypeRepository;
        this.authUtils = authUtils;
    }

    @GetMapping
    public String listRequests(Model model) {
        String email = authUtils.getCurrentUserEmail();
        model.addAttribute("requests", requestService.getCitizenRequests(email));
        return "requests";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("createRequest", new CreateRequestRequest());
        model.addAttribute("requestTypes", requestTypeRepository.findAll());
        return "request-form";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("createRequest") CreateRequestRequest dto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestTypes", requestTypeRepository.findAll());
            return "request-form";
        }

        String email = authUtils.getCurrentUserEmail();
        requestService.createRequest(email, dto);
        return "redirect:/requests";
    }
}
