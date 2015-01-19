package uk.ac.imperial.libhpc2.schemaservice.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;

@Controller
public class RootController {

	private static final Logger sLog = LoggerFactory.getLogger(RootController.class.getName());
	
	@Autowired
	private ProfileDao profileDao;
	
	@RequestMapping("/")
    public ModelAndView index(Model pModel) {
		
		sLog.debug("Processing root controller request for access to /");
		
        ModelAndView mav = new ModelAndView("index");
        
        List<Profile> profiles = profileDao.findAll();
        
        mav.addObject("firstname", "Tempro");
        mav.addObject("surname", "Team");
        mav.addObject("profiles", profiles);
        return mav;
    }
}
