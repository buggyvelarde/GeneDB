package org.genedb.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.gmod.schema.mapped.Feature;

import com.hazelcast.core.Hazelcast;

@Aspect
public class HazelcastAspect {
	
	public static final String dtoMapName = "dtoMap";
	
	private static final Logger logger = Logger.getLogger(HazelcastAspect.class);

	@Around("execution(* getDtoByName(..))")
	public Object getFromHazelcast(ProceedingJoinPoint pjp) 
		throws Throwable {
		
		Object[] args = pjp.getArgs();
		
		Feature feature = (Feature ) args[0];
		
		TranscriptDTO dto = (TranscriptDTO) Hazelcast.getMap(dtoMapName).get(feature.getUniqueName());
		
		if (dto != null) {
			logger.info("***** Found " + feature.getUniqueName() + " in Hazelcast distributed map!");
			return dto;
		}
		
		logger.info("***** Could not find DTO for " + feature.getUniqueName());
		
		return pjp.proceed();
	}
	
	@AfterReturning(pointcut="execution(* saveDto(..))", returning="retVal")
	public void putIntoHazelcast(Object retVal) {
		TranscriptDTO dto = (TranscriptDTO)retVal;
		
		logger.info("***** Storing  " + dto.getUniqueName());
		Hazelcast.getMap(dtoMapName).put(dto.getUniqueName(), dto);
	}

}
