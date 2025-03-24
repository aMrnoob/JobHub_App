package com.example.befindingjob.service.impl;

import com.example.befindingjob.repository.JobRepository;
import com.example.befindingjob.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillServiceImpl implements SkillService {

    @Autowired
    private JobRepository jobRepository;


}
