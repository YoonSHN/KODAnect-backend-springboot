package kodanect.domain.donation.repository;


import kodanect.domain.donation.entity.entity.DonationStoryComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationCommentRepository extends JpaRepository<DonationStoryComment, Long> {

}

