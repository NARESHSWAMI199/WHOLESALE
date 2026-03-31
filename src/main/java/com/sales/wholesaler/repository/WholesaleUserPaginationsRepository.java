package com.sales.wholesaler.repository;


import com.sales.entities.UserPagination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholesaleUserPaginationsRepository extends JpaRepository<UserPagination, Integer> {
    @Query(value = """
            from UserPagination up
                 left join Pagination p on p.id = up.pagination .id
             where (p.canSee = 'W' or p.canSee = 'B') and up.userId = :userId 
             order by p.id""")
    List<UserPagination> getUserPaginationByUserId(@Param("userId") Integer userId);
}
