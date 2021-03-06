/**
 * 
 */
package com.ymt.mirage.car.service.impl;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.ymt.pz365.data.jpa.support.AbstractDomain2InfoConverter;
import org.springframework.transaction.annotation.Transactional;

import com.ymt.mirage.car.domain.Activity;
import com.ymt.mirage.car.domain.KeyWord;
import com.ymt.mirage.car.domain.ParticipationType;
import com.ymt.mirage.car.dto.ActivityInfo;
import com.ymt.mirage.car.repository.ActivityRepository;
import com.ymt.mirage.car.repository.CustomerServiceRepository;
import com.ymt.mirage.car.repository.KeyWordRepository;
import com.ymt.mirage.car.repository.SponsorRepository;
import com.ymt.mirage.car.repository.spec.ActivitySpec;
import com.ymt.mirage.car.service.ActivityService;
import com.ymt.pz365.data.jpa.support.QueryResultConverter;
import com.ymt.pz365.framework.core.exception.PzException;

/**
 * @author zhailiang
 * @since 2016年6月5日
 */
@Service("activityService")
@Transactional
public class ActivityServiceImpl implements ActivityService {

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private CustomerServiceRepository customerServiceRepository;

	@Autowired
	private SponsorRepository sponsorRepository;

	@Autowired
	private KeyWordRepository keyWordRepository;

	@Override
	public Page<ActivityInfo> query(ActivityInfo activityInfo, Pageable pageable) {
//		Page<Activity> pageData = activityRepository.findAll(new ActivitySpec(activityInfo), pageable);
//		return QueryResultConverter.convert(pageData, ActivityInfo.class, pageable);

		Page<Activity> pageData = activityRepository.findAll(new ActivitySpec(activityInfo), pageable);
		return QueryResultConverter.convert(pageData, pageable,
				new AbstractDomain2InfoConverter<Activity, ActivityInfo>() {
					@Override
					protected void doConvert(Activity domain, ActivityInfo info) throws Exception {
						info.setSponsor(domain.getSponsor().getSponsor());
					}
				});
	}

	@Override
	public ActivityInfo create(ActivityInfo activityInfo) {
		Activity activity = new Activity();
		if (new DateTime(activityInfo.getStartTime()).isBefore(new DateTime(activityInfo.getEndTime()))) {
			throw new PzException("开始时间不能早于报名截止时间");
		}
		BeanUtils.copyProperties(activityInfo, activity);
		activity.setType(ParticipationType.ACTIVITY);
		activity.setCustomerService(customerServiceRepository.findOne(activityInfo.getCustomerServiceId()));
		activity.setSponsor(sponsorRepository.findOne(activityInfo.getSponsorId()));

		List<KeyWord> kwlist = activityInfo.getKwlist();
		for (KeyWord keyWord : kwlist) {
			keyWordRepository.save(keyWord);
		}
		activityInfo.setSponsor(activity.getSponsor().getSponsor());
		activityInfo.setId(activityRepository.save(activity).getId());
		return activityInfo;
	}

	@Override
	public ActivityInfo getInfo(Long id) {
		Activity activity = activityRepository.findOne(id);
		ActivityInfo info = new ActivityInfo();
		BeanUtils.copyProperties(activity, info);
		return info;
	}

	@Override
	public ActivityInfo update(ActivityInfo activityInfo) {
		Activity activity = activityRepository.findOne(activityInfo.getId());
		BeanUtils.copyProperties(activityInfo, activity);
		activity.setType(ParticipationType.ACTIVITY);
		activityRepository.save(activity);
		return activityInfo;
	}

	@Override
	public void delete(Long id) {
		activityRepository.delete(id);
	}

}
