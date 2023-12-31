package com.example.stock.scheduler;

import com.example.stock.model.Company;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constants.CacheKey;
import com.example.stock.persist.CompanyRepository;
import com.example.stock.persist.DividendRepository;
import com.example.stock.persist.entity.CompanyEntity;
import com.example.stock.persist.entity.DividendEntity;
import com.example.stock.scrapper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinancialScraper;
    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){

        // 저장된 회사 목록을 조회
        // spring batch -> 대용량일때 유용
        List< CompanyEntity> companies =this.companyRepository.findAll();
        // 회사마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            log.info("scraping scheduler is started"+company.getName());
            ScrapedResult scrapedResult =
                    this.yahooFinancialScraper.scrap(
                            new Company(company.getName(),company.getTicker()));
            // 스크래핑한 배당금 정보 등 디비에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // dividend model -> entity
                    .map( e ->new  DividendEntity(company.getId(), e))
                    .forEach(e ->{
                        // 하나하나 dividend repository 에 삽입
                        boolean exists = this.dividendRepository
                                .existsByCompanyIdAndDate(e.getCompanyId(),e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend ->"+e.toString());
                        }
                    });
            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
