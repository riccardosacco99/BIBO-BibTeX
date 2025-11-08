# Sprint Planning Guide - How to Use These Documents

- **For:** BIBO-BibTeX Converter Project
- **Created:** 2025-11-07
- **Purpose:** Guide to using the agile sprint planning documents

---

## 📁 Document Structure

Sprint planning is organized in these files:

```
BIBO-BibTeX/
├── ROADMAP.md                    # High-level project timeline and vision
├── sprint-backlog.md             # Complete product backlog with all user stories
├── sprint-00-hot-fixes           # Sprint 00 detailed plan
├── sprint-01-critical-mvp.md     # Sprint 01 detailed plan
├── sprint-02-production-quality.md   # Sprint 02 detailed plan
├── sprint-03-advanced-features.md    # Sprint 03 detailed plan
└── SPRINT_PLANNING_GUIDE.md      # This file - how to use everything
```

---

## 🎯 Quick Start - How to Begin

### 1. **Read the ROADMAP**
   - Understand the overall project timeline
   - See what's completed vs. what's ahead
   - Review success metrics and targets
   - Check risk assessment

### 2. **Review Sprint 00 Plan**
   - Read each FIX section (01–04) carefully to understand the problem, tasks, and acceptance criteria; focus on architectural implications of FIX-01 and FIX-04
   - Identify dependencies: FIX-01 unblocks all others; FIX-03 and FIX-04 depend on updated RDF model; FIX-02 can run in parallel

### 3. **Review Sprint 01 Plan**
   - Read each user story carefully
   - Understand acceptance criteria
   - Note dependencies

### 4. **Start with Highest Priority**
   - Begin with **US-02 (Validation)** - it's foundational
   - Then move to **US-01 (VocBench Integration)**
   - Follow the priority order in each sprint

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

### sprint-01-critical-mvp.md

**When to read:**
- Daily during Sprint 01
- At sprint review time

**What to use it for:**
- Daily work plan
- Task tracking
- Acceptance criteria verification
- Definition of Done checks

**How to work through it:**

1. **US-02 Input Validation**
   ```
   [ ] Create BibliographicConversionException
   [ ] Add validateBibTeXEntry() method
   [ ] Write 10+ validation tests
   [ ] Update JavaDoc
   ```

2. **US-01 VocBench Integration**
   ```
   [ ] Create RDF4JRepositoryGateway
   [ ] Implement store() method
   [ ] Implement fetch() method
   [ ] Write integration tests
   ```

3. **US-03 Extended Fields**
   ```
   [ ] Add series, edition, keywords to model
   [ ] Update converter
   [ ] Write tests
   ```

4. **US-04 Edge Case Testing**
   ```
   [ ] Create edge case test class
   [ ] Write 30+ edge case tests
   [ ] Run coverage report
   ```

5. **US-05 Round-Trip Testing**
   ```
   [ ] Create round-trip test class
   [ ] Test all document types
   ```

6. **US-06 Documentation**
   ```
   [ ] Create FIELD_MAPPING.md
   [ ] Add examples to README
   ```

7. **US-07 Configuration + Buffer**
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

### sprint-02-production-quality.md

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

## 📈 Progress Tracking

### GitHub Issues

Create GitHub issues for each user story:
- Issue title: "US-02: Robust Input Validation"
- Labels: `enhancement`, `sprint-01`, `priority-p0`
- Track in GitHub Projects board

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

1. **Research**
   - Google the issue
   - Check StackOverflow
   - Read documentation

2. **Try Alternative Approach**
   - Is there a simpler way?
   - Can you use a library?
   - Can you defer complexity?

3. **Ask for Help**
   - Supervisor/advisor
   - Online forums
   - VocBench community
   - AI assistant

4. **Document and Move On**
   - Note the blocker in sprint file
   - Work on another task
   - Come back later with fresh perspective

---

## 🔄 Sprint Review Process

### End of Each Sprint

1. **Review Completed Work**
   - Go through each user story
   - Mark as complete or carry over
   - Calculate velocity (story points completed)

2. **Demo Functionality**
   - Show working features to yourself/supervisor
   - Record demo video for thesis
   - Note any feedback

3. **Update Documents**
   - Mark completed stories in backlog
   - Update ROADMAP.md status
   - Note learnings and adjustments

4. **Retrospective**
   - What went well?
   - What could be improved?
   - What will you change next sprint?

5. **Plan Next Sprint**
   - Review next sprint plan
   - Adjust based on velocity
   - Identify any new dependencies

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

## Resources
**Build & Test:**
- Maven 3.x
- JUnit 5
- JaCoCo for coverage
- Surefire for test reports

**Documentation:**
- JavaDoc (built-in)
- Markdown editor (Typora, VS Code)

**Project Management:**
- GitHub Projects

**Version Control:**
- Git
- GitHub/GitLab

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

- **Last Updated:** 2025-11-07
- **Maintainer:** Riccardo Sacco
- **Version:** 1.0
