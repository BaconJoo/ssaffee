package coffee.ssafy.ssafee.domain.party.service;

import coffee.ssafy.ssafee.domain.party.dto.request.OrderMenuRequest;
import coffee.ssafy.ssafee.domain.party.dto.response.OrderMenuResponse;
import coffee.ssafy.ssafee.domain.party.entity.*;
import coffee.ssafy.ssafee.domain.party.mapper.OrderMenuMapper;
import coffee.ssafy.ssafee.domain.party.repository.OrderMenuRepository;
import coffee.ssafy.ssafee.domain.party.repository.ParticipantRepository;
import coffee.ssafy.ssafee.domain.shop.entity.Menu;
import coffee.ssafy.ssafee.domain.shop.entity.Option;
import coffee.ssafy.ssafee.domain.shop.entity.OptionCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderMenuService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final PartyService partyService;
    private final ParticipantRepository participantRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final OrderMenuMapper orderMenuMapper;

    public Long createOrderMenu(String accessCode, OrderMenuRequest orderMenuRequest) {
        Long partyId = partyService.findPartyIdByAccessCode(accessCode);
        Party party = entityManager.getReference(Party.class, partyId);
        Menu menuReference = entityManager.getReference(Menu.class, orderMenuRequest.menuId());

        Participant participant = participantRepository.findByPartyIdAndName(partyId, orderMenuRequest.participantName())
                .orElseGet(() -> Participant.builder()
                        .name(orderMenuRequest.participantName())
                        .party(party)
                        .build());
        participantRepository.save(participant);

        OrderMenu orderMenu = OrderMenu.builder()
                .menu(menuReference)
                .participant(participant)
                .party(party)
                .build();
        orderMenu.setOrderMenuOptionCategories(orderMenuRequest.optionCategories().stream()
                .map(chosenOptionCategoryRequest -> {
                    OrderMenuOptionCategory orderMenuOptionCategory = OrderMenuOptionCategory.builder()
                            .orderMenu(orderMenu)
                            .optionCategory(entityManager.getReference(OptionCategory.class, chosenOptionCategoryRequest.optionCategoryId()))
                            .build();
                    orderMenuOptionCategory.setOrderMenuOptions(chosenOptionCategoryRequest.optionIds().stream()
                            .map(optionId -> OrderMenuOption.builder()
                                    .orderMenuOptionCategory(orderMenuOptionCategory)
                                    .option(entityManager.getReference(Option.class, optionId))
                                    .build())
                            .toList());
                    return orderMenuOptionCategory;
                })
                .toList());
        orderMenuRepository.save(orderMenu);
        return orderMenu.getId();
    }

    public List<OrderMenuResponse> findOrderMenusByAccessCode(String accessCode) {
        Long partyId = partyService.findPartyIdByAccessCode(accessCode);
        return orderMenuRepository.findAllByPartyId(partyId).stream()
                .map(orderMenuMapper::toDto)
                .toList();
    }

    public void deleteOrderMenuByAccessCodeAndId(String accessCode, Long orderMenuId) {
        Long partyId = partyService.findPartyIdByAccessCode(accessCode);
        orderMenuRepository.deleteByPartyIdAndId(partyId, orderMenuId);
    }

}
