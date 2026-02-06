package com.sma.core.repository;

import com.sma.core.entity.Resume;
import com.sma.core.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SkillRepository  extends JpaRepository<Skill, Integer> {

}
