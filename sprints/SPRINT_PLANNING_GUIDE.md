# Sprint Planning Guide - How to Use These Documents

**For:** BIBO-BibTeX Converter Project
**Created:** 2025-11-07
**Purpose:** Guide to using the agile sprint planning documents

---

## 📁 Document Structure

Your sprint planning is organized in these files:

```
BIBO-BibTeX/
├── ROADMAP.md                    # High-level project timeline and vision
├── sprint-backlog.md             # Complete product backlog with all user stories
├── sprint-01-critical-mvp.md     # Sprint 01 detailed plan (3 weeks)
├── sprint-02-production-quality.md   # Sprint 02 detailed plan (2 weeks)
├── sprint-03-advanced-features.md    # Sprint 03 detailed plan (2-3 weeks)
└── SPRINT_PLANNING_GUIDE.md      # This file - how to use everything
```

---

## 🎯 Quick Start - How to Begin

### 1. **Read the ROADMAP First** (15 minutes)
   - Understand the overall project timeline
   - See what's completed vs. what's ahead
   - Review success metrics and targets
   - Check risk assessment

### 2. **Review Sprint 01 Plan** (30 minutes)
   - Read each user story carefully
   - Understand acceptance criteria
   - Note dependencies
   - Estimate your own capacity

### 3. **Start with Highest Priority** (Day 1)
   - Begin with **US-02 (Validation)** - it's foundational
   - Then move to **US-01 (VocBench Integration)**
   - Follow the priority order in each sprint

### 4. **Track Your Progress Daily**
   - Check off completed tasks `[ ]` → `[x]`
   - Update story status
   - Note any blockers or issues

---

## 📊 How to Use Each Document

### ROADMAP.md - The Big Picture

**When to read:**
- At project start
- Beginning of each sprint
- When making strategic decisions
- For thesis documentation

**What to use it for:**
- Understanding project timeline
- Communicating progress to supervisor
- Making scope decisions
- Planning demos/presentations

**Key Sections:**
- **Current Status:** See what's done
- **Release Roadmap:** Understand milestones
- **Feature Matrix:** Compare versions
- **Critical Path:** Know blocking dependencies

---

### sprint-backlog.md - The Product Backlog

**When to read:**
- Sprint planning sessions
- When prioritizing work
- When adding new features
- For estimation reference

**What to use it for:**
- All user stories in one place
- Story point estimates
- Epic organization
- Future planning

**Key Sections:**
- **Backlog Items by Epic:** Organized features
- **Future Backlog:** Post-1.0 ideas
- **Technical Debt:** Things to fix
- **Bug Tracking:** Known issues

**How to maintain:**
- Add new stories as they arise
- Update priorities based on learnings
- Mark stories as complete
- Refine estimates after each sprint

---

### sprint-01-critical-mvp.md - Your First Sprint

**When to read:**
- NOW - This is your immediate work
- Daily during Sprint 01
- At sprint review time

**What to use it for:**
- Daily work plan
- Task tracking
- Acceptance criteria verification
- Definition of Done checks

**How to work through it:**

1. **Day 1-2: US-02 Input Validation**
   ```
   [ ] Create BibliographicConversionException
   [ ] Add validateBibTeXEntry() method
   [ ] Write 10+ validation tests
   [ ] Update JavaDoc
   ```

2. **Day 3-7: US-01 VocBench Integration**
   ```
   [ ] Create RDF4JRepositoryGateway
   [ ] Implement store() method
   [ ] Implement fetch() method
   [ ] Write integration tests
   ```

3. **Day 8-10: US-03 Extended Fields**
   ```
   [ ] Add series, edition, keywords to model
   [ ] Update converter
   [ ] Write tests
   ```

4. **Day 11-14: US-04 Edge Case Testing**
   ```
   [ ] Create edge case test class
   [ ] Write 30+ edge case tests
   [ ] Run coverage report
   ```

5. **Day 15-16: US-05 Round-Trip Testing**
   ```
   [ ] Create round-trip test class
   [ ] Test all document types
   ```

6. **Day 17-18: US-06 Documentation**
   ```
   [ ] Create FIELD_MAPPING.md
   [ ] Update CLAUDE.md
   [ ] Add examples to README
   ```

7. **Day 19-21: US-07 Configuration + Buffer**
   ```
   [ ] Create plugin.properties
   [ ] Implement configuration loader
   [ ] Final testing and cleanup
   ```

**Sprint Review Preparation:**
- Demo VocBench plugin importing entries
- Show test coverage report
- Present field mapping docs
- List completed vs. remaining work

---

### sprint-02-production-quality.md - Second Sprint

**When to read:**
- After Sprint 01 review
- For planning next steps
- When refining estimates

**Focus:**
- Code quality improvements
- Robustness and edge cases
- Documentation completeness
- Performance optimization

**When to start:**
- After completing Sprint 01
- Or: Start earlier if ahead of schedule
- Or: Defer if Sprint 01 slips

---

### sprint-03-advanced-features.md - Third Sprint

**When to read:**
- After Sprint 02 completion
- For planning VocBench UI work
- For production deployment prep

**Focus:**
- VocBench UI integration
- Advanced features (duplicates, conflicts)
- Production packaging
- Security and deployment

**When to start:**
- After Sprint 01 and 02
- May split into multiple sprints if needed
- Some stories can be deferred to v1.1

---

## ✅ Daily Workflow

### Morning (30 mins)
1. **Review yesterday's progress**
   - What did you complete?
   - Any blockers?

2. **Plan today's tasks**
   - Which user story are you working on?
   - What specific tasks will you tackle?

3. **Update sprint file**
   - Check off completed items `[x]`
   - Add notes on blockers

### During Work
1. **Follow TDD approach**
   - Write test first
   - Implement code
   - Refactor

2. **Commit frequently**
   - Small, focused commits
   - Clear commit messages
   - Reference user story (e.g., "US-02: Add validation for...")

3. **Update documentation**
   - Add JavaDoc as you code
   - Update CLAUDE.md if architecture changes

### Evening (15 mins)
1. **Update sprint tracking**
   - Mark completed tasks
   - Note any new tasks discovered

2. **Reflect on progress**
   - Are you on track?
   - Need to adjust priorities?

3. **Prepare for tomorrow**
   - What's next?
   - Any research needed overnight?

---

## 📈 Progress Tracking

### Option 1: Markdown Checkboxes (Simple)

Just check off items in the sprint file:

```markdown
- [x] Create validation exception class
- [x] Add validateBibTeXEntry() method
- [ ] Write validation tests
- [ ] Update JavaDoc
```

### Option 2: GitHub Issues (Advanced)

Create GitHub issues for each user story:
- Issue title: "US-02: Robust Input Validation"
- Labels: `enhancement`, `sprint-01`, `priority-p0`
- Assign to yourself
- Track in GitHub Projects board

### Option 3: Trello/Jira (Team Setting)

If working with others:
- Create board with columns: Backlog, Sprint, In Progress, Review, Done
- Create cards for each user story
- Move cards as you progress

---

## 🎯 Acceptance Criteria Checklist

Before marking a user story as DONE, verify:

```
US-XX: [Story Name]

✅ DEVELOPMENT
[ ] All tasks completed
[ ] Code reviewed (self or peer)
[ ] No commented-out code
[ ] No debug statements left

✅ TESTING
[ ] Unit tests written (>70% coverage)
[ ] All tests passing
[ ] Edge cases tested
[ ] Integration tests (if applicable)

✅ DOCUMENTATION
[ ] JavaDoc added/updated
[ ] CLAUDE.md updated (if needed)
[ ] README updated (if needed)
[ ] Examples updated (if needed)

✅ QUALITY
[ ] No critical/high bugs
[ ] Build passing (mvn clean package)
[ ] Code formatted consistently
[ ] No new warnings

✅ ACCEPTANCE CRITERIA
[ ] All acceptance criteria met
[ ] Demo-able functionality
[ ] Meets Definition of Done
```

---

## 🚧 Handling Blockers

### If You Get Stuck

1. **Research (30 mins)**
   - Google the issue
   - Check StackOverflow
   - Read documentation

2. **Try Alternative Approach (1 hour)**
   - Is there a simpler way?
   - Can you use a library?
   - Can you defer complexity?

3. **Ask for Help**
   - Supervisor/advisor
   - Online forums
   - VocBench community
   - Claude Code AI assistant

4. **Document and Move On**
   - Note the blocker in sprint file
   - Work on another task
   - Come back later with fresh perspective

### Example Blocker Note

```markdown
## BLOCKER: US-01 VocBench Integration

**Issue:** Cannot find VocBench plugin API documentation
**Impact:** Blocking UI integration
**Attempted:**
- Searched VocBench docs
- Checked GitHub repos
- Asked on mailing list

**Action:** Emailed VocBench team, waiting for response
**Workaround:** Using RDF4J directly for now, will refactor later
**Updated:** 2025-11-08
```

---

## 🔄 Sprint Review Process

### End of Each Sprint

1. **Review Completed Work** (1 hour)
   - Go through each user story
   - Mark as complete or carry over
   - Calculate velocity (story points completed)

2. **Demo Functionality** (30 mins)
   - Show working features to yourself/supervisor
   - Record demo video for thesis
   - Note any feedback

3. **Update Documents** (30 mins)
   - Mark completed stories in backlog
   - Update ROADMAP.md status
   - Note learnings and adjustments

4. **Retrospective** (30 mins)
   - What went well?
   - What could be improved?
   - What will you change next sprint?

5. **Plan Next Sprint** (1 hour)
   - Review next sprint plan
   - Adjust based on velocity
   - Identify any new dependencies

### Sprint Review Template

```markdown
# Sprint 01 Review - [Date]

## Completed (Story Points: XX/46)
- [x] US-02: Input Validation (5 pts)
- [x] US-01: VocBench Integration (13 pts)
- [ ] US-03: Extended Fields (8 pts) - 80% done

## Carried Over
- US-03: Extended Fields (1 day remaining)
- US-07: Configuration (pushed to Sprint 02)

## Velocity
- Planned: 46 story points
- Completed: 38 story points
- Velocity: 83% (adjust Sprint 02 expectations)

## What Went Well
- VocBench integration easier than expected
- Test coverage already at 72%
- Good progress on validation

## What Could Improve
- Underestimated documentation time
- Need better time tracking
- Should have researched VocBench earlier

## Learnings
- RDF4J API is well-documented
- Name parsing more complex than thought
- Configuration should be simpler

## Next Sprint Adjustments
- Reduce Sprint 02 scope by 5 story points
- Allocate more time for research tasks
- Schedule daily progress reviews
```

---

## 📝 Estimation Guide

### Story Point Scale

```
1 point:   Trivial (< 2 hours)
           - Add simple field
           - Update documentation
           - Fix typo

2 points:  Simple (2-4 hours)
           - Add new enum value
           - Write basic test
           - Update configuration

3 points:  Moderate (4-8 hours)
           - Add new field with mapping
           - Create new test class
           - Write documentation section

5 points:  Complex (1-2 days)
           - New feature with tests
           - Refactor component
           - Comprehensive test suite

8 points:  Very Complex (2-3 days)
           - Major feature implementation
           - Integration work
           - Multiple component changes

13 points: Epic (3-5 days)
           - Large feature with integration
           - Requires research
           - Multiple dependencies

21 points: Too Large (break down!)
           - Split into smaller stories
```

### Re-estimation

After each sprint, refine estimates based on actual time:
- Completed faster? Lower estimate
- Took longer? Increase estimate
- Use actual data to improve future planning

---

## 🎓 Thesis Integration

### Using Sprints for Thesis

**Chapter 4: Implementation**
- Use sprint structure for narrative
- Show iterative development process
- Demonstrate agile methodology

**Sprint 01 → Thesis Section 4.1: Core Implementation**
- VocBench integration
- Validation framework
- Extended field support

**Sprint 02 → Thesis Section 4.2: Quality Improvements**
- Comprehensive type support
- Robust parsing
- Performance optimization

**Sprint 03 → Thesis Section 4.3: Production Deployment**
- UI integration
- Advanced features
- Deployment preparation

### Evidence for Thesis

From each sprint, collect:
- ✅ Test coverage reports
- ✅ Performance benchmarks
- ✅ Code quality metrics
- ✅ Demo screenshots/videos
- ✅ Documentation artifacts
- ✅ Sprint review notes

---

## 🛠️ Tools & Resources

### Recommended Tools

**IDE:**
- IntelliJ IDEA (recommended)
- Eclipse with Maven plugin
- VS Code with Java extensions

**Build & Test:**
- Maven 3.x
- JUnit 5
- JaCoCo for coverage
- Surefire for test reports

**Documentation:**
- JavaDoc (built-in)
- Markdown editor (Typora, VS Code)
- Draw.io for diagrams

**Project Management:**
- GitHub Projects (free)
- Trello (simple)
- Jira (if available)

**Version Control:**
- Git
- GitHub/GitLab
- Commit often!

### Useful Commands

```bash
# Run specific sprint's tests
mvn test -Dtest=BibliographicConverterTest

# Generate coverage report
mvn clean test jacoco:report
# View: target/site/jacoco/index.html

# Generate JavaDoc
mvn javadoc:javadoc
# View: target/site/apidocs/index.html

# Package for deployment
mvn clean package

# Run example
mvn exec:java -pl core -Dexec.mainClass="...SampleConversion"
```

---

## 📞 Getting Help

### When to Ask

- **Immediately:** Blockers affecting critical path
- **Within 1 day:** Technical questions on VocBench
- **Within 1 week:** Clarifications on requirements

### Where to Ask

1. **Thesis Supervisor:** Academic guidance, scope decisions
2. **VocBench Community:** Integration questions
3. **StackOverflow:** Technical Java/RDF4J questions
4. **Claude Code:** Implementation help, code review

### How to Ask

**Good Question:**
```
I'm implementing US-01 (VocBench Integration) and need to
store RDF models in the repository. I've tried using
RepositoryConnection.add(model) but get an error:

[error message]

My code:
[code snippet]

What I've tried:
- Checked RDF4J docs
- Verified connection is open
- Tested with simple model

What's the correct approach?
```

**Poor Question:**
```
VocBench doesn't work. Help?
```

---

## 🎉 Success Criteria

### Sprint 01 Success
- [ ] 7+ user stories completed
- [ ] VocBench plugin functional
- [ ] Test coverage > 70%
- [ ] Documentation complete
- [ ] Ready for thesis demo

### Overall Success
- [ ] All sprints completed
- [ ] v1.0.0 released
- [ ] Thesis defended successfully
- [ ] Plugin deployed in VocBench
- [ ] Documentation published

---

## 📅 Schedule Template

### Weekly Schedule

**Monday:**
- Sprint planning (if new sprint)
- Review last week's progress
- Set weekly goals

**Tuesday-Thursday:**
- Focused development time
- Daily progress updates
- Address blockers

**Friday:**
- Code review and cleanup
- Update documentation
- Plan next week
- Weekly reflection

### Daily Schedule (Suggested)

```
09:00-09:30  Review sprint plan, set daily goals
09:30-12:00  Deep work - coding
12:00-13:00  Lunch break
13:00-15:00  Testing, documentation
15:00-15:30  Break
15:30-17:30  Continue work, research
17:30-18:00  Update sprint tracking, plan tomorrow
```

---

## 🎯 Remember

1. **Start with US-02 (Validation)** - it's foundational
2. **Follow the priority order** - it's optimized
3. **Check off tasks daily** - stay motivated
4. **Don't skip tests** - they save time later
5. **Document as you go** - don't leave it for the end
6. **Ask for help early** - don't waste days stuck
7. **Celebrate progress** - acknowledge completed stories
8. **Be flexible** - adjust plans based on reality

---

## 🚀 Ready to Start?

**Next Actions:**
1. ✅ Read this guide (you're here!)
2. ⏳ Review ROADMAP.md
3. ⏳ Open sprint-01-critical-mvp.md
4. ⏳ Start US-02 (Validation)
5. ⏳ Make your first commit

**Good luck with your thesis project!** 🎓

---

*Questions about this guide? Add them to the bottom of this file for future reference.*

---

## FAQ

**Q: Do I have to follow sprints exactly?**
A: No, they're guidelines. Adjust based on your progress and constraints.

**Q: What if I fall behind?**
A: Prioritize ruthlessly. Focus on P0/P1 items. Defer P3 items to future.

**Q: What if I finish early?**
A: Great! Start next sprint or add polish (more tests, better docs).

**Q: Should I work on multiple stories at once?**
A: No, focus on one at a time. Finish completely before starting next.

**Q: What if estimates are wrong?**
A: That's normal! Update estimates and adjust future planning.

**Q: Can I skip tests?**
A: No. Tests are critical for thesis defense and confidence in code.

**Q: What if VocBench integration is too hard?**
A: Focus on core converter for thesis. VocBench can be simpler integration.

**Q: When should I start writing thesis?**
A: Document as you go! Each sprint = thesis section. Write weekly summaries.

---

**Last Updated:** 2025-11-07
**Maintainer:** Riccardo Sacco
**Version:** 1.0
